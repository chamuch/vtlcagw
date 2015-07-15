package com.satnar.smpp.transport;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.satnar.common.LogService;
import com.satnar.smpp.StackMap;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.pdu.ParsingDelegate;

public class ReadHelper implements Runnable {
    
    private boolean         canRun         = true;
    private Connection      smppConnection = null;
    private int             threadPoolSize = 0;
    private ExecutorService processorPool  = null;   
    
    
    public ReadHelper(Connection connection) {
        this.smppConnection = connection;
        this.threadPoolSize = this.smppConnection.getThreadPoolSize();
        
        this.processorPool = new ThreadPoolExecutor((this.threadPoolSize/4),    // initial pool size
                this.threadPoolSize,                                            // max pool size
                30000,                                                          // keep alive time
                TimeUnit.MILLISECONDS,                                          // keep alive time unit (set to 30 secs)
                new LinkedBlockingQueue<Runnable>((this.threadPoolSize/2)));    // wait queue size
    }
    
    public void run() {
        LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - SMPP Connection State: " + this.smppConnection.getConnectionState());
        while (this.canRun) {
            
            if ( this.smppConnection.getConnectionState() != SmppSessionState.CLOSED ||
                    this.smppConnection.getConnectionState() != SmppSessionState.UNBOUND) {
                
                boolean networkConnectionState = ((TcpConnection)this.smppConnection).getConnection().isConnected();
                LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Current SMPP Connection State: " + networkConnectionState);
                if (!networkConnectionState) {
                    LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - ReadHelper-run: Socket already seems to be broken. Proactive & Preventive measure to shutdown stack!!");
                    Esme session = StackMap.getStack(this.smppConnection.getEsmeLabel());
                    session.stop();
                }
                
                ByteBuffer readBuffer = this.smppConnection.getResponseBuffer();
                
                synchronized (readBuffer) {
                    try {
                        this.smppConnection.read(readBuffer);
                        readBuffer.flip();
                        
                    } catch (SmppTransportException e) {
                        if (e.getCause() != null) {
                            this.canRun = false;
                            LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - ReadHelper-run: Encountered Exception, underlying socket seems broken...:",e);
                            // underlying socket seems broken...                    	
                            Esme session = StackMap.getStack(this.smppConnection.getEsmeLabel());
                            if (session != null) session.stop();
                        }
                    }
                    
                    // check the buffer...
                    int windowSize = readBuffer.remaining();   LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Read Buffer Window Size read: " + windowSize);
                    byte[] currentWindow = new byte[windowSize];
                    
                    if (windowSize > 4) {
                        readBuffer.get(currentWindow);
                        readBuffer.clear();
                        LogService.stackTraceLog.debug(this.smppConnection.getEsmeLabel() + " - Current TCP Window: " + prettyPrint(currentWindow));
                        
                        
                        // prepare decoding stream for the current window...
                        ByteArrayInputStream baisWindow = new ByteArrayInputStream(currentWindow);
                        DataInputStream parser = new DataInputStream(baisWindow);
                        
                        // check for complete PDU
                        try {
                            do {
                                int pduLength = parser.readInt();
                                int remaining = parser.available();
                                if ((pduLength - 4) <= remaining) {
                                    byte[] pduPayload = new byte[pduLength - 4]; // since 4 bytes is already read for PDU length
                                    parser.read(pduPayload);
                                    LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Complete PDU available for read. Size: " + pduLength);
                                    LogService.stackTraceLog.debug(this.smppConnection.getEsmeLabel() + " - Decoding Delgate for PDU: " + prettyPrint(pduPayload));
                                    
                                    
                                    //TODO: delegate to facade
                                    try {
                                        ParsingDelegate switchingDelegator = new ParsingDelegate(pduPayload, this.smppConnection.getMode());
                                        this.processorPool.submit(switchingDelegator);
                                        LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - PDU handed over to facade in threadpool");
                                    } catch (RejectedExecutionException e) {
                                        LogService.appLog.error(this.smppConnection.getEsmeLabel() + " - Unable to handover PDU into facade. [Pretty Print the PDU later] Reason: " + e.getMessage());
                                    }
                                    
                                } else {
                                    // this is the sliding window
                                    LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Sliding Window remaining buffer: " + remaining);
                                    byte[] partialPayload = new byte[remaining];
                                    readBuffer.putInt(pduLength);
                                    readBuffer.put(partialPayload);
                                    break;
                                }
                            } while (true);
                            LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Current Packet processed, lets wait for next...");
                            
                        } catch (IOException e) {
                            this.canRun = false;
                            LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - ReadHelper-run: whatever pending in this window has gone bad. Dumping the current window. Potentially subsequent windows will fail too:",e);
                            Esme session = StackMap.getStack(this.smppConnection.getEsmeLabel());
                            LogService.appLog.info("Checking Smpp Session: " + this.smppConnection.getEsmeLabel() + " in session store: " + (session != null));
                            if (session != null) session.stop();
                        } // end of try block
                    } // end of window size
                    else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // do nothing
                        } // end of catch
                    } // end of window size
                } // end of sync block
            } // end of if to avoid accidental closure of loop
        } //  end of thread body event loop
        
        try {
            this.processorPool.shutdownNow();
            this.processorPool.awaitTermination(3, TimeUnit.SECONDS);
            this.processorPool = null;
        } catch (InterruptedException e) {
            LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - ReadHelper-run:Encountered Exception",e);
        }
    }
    
    public void stop() {
        this.canRun = false;
        LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - ReadHelper-stop: Stopping the read helper thread!!");
    }
    
    
    private String prettyPrint(byte[] serialized) {
        StringBuilder sbPrettyPrint = new StringBuilder();
        for (byte atom: serialized) {
            sbPrettyPrint.append(Integer.toHexString(atom));
            sbPrettyPrint.append(" ");
        }
        return sbPrettyPrint.toString();
    }
    
    
}
