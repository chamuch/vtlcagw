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
        SlidingWindowBuffer slidingWindow = new SlidingWindowBuffer();
        
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
                
                int windowSize = 0;
                try {
                    byte[] currentWindow = null;
                    synchronized (readBuffer) {
                        windowSize =  this.smppConnection.read(readBuffer);
                        
                        // check the incoming packet size...
                        if (windowSize > 0) {
                            LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Read Buffer Window Size read: " + windowSize);
                            currentWindow = new byte[windowSize];
                            readBuffer.get(currentWindow); readBuffer.clear();
                        } else { // else, lets wait 0.5 sec (this can be optimized later)
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                // do nothing
                            } // end of catch
                        } // end of window size
                    }
                    
                    // adjust the sliding window
                    slidingWindow.push(currentWindow);
                    DataInputStream parser = new DataInputStream(slidingWindow.getConsumingStream());
                    
                    // process the burst...
                    do {
                        if (parser.available() > 4) { // to ensure we have enough bytes to atleast decode PDU length...
                            int pduLength = parser.readInt();
                            if ( (pduLength - 4) <= parser.available() ){
                                byte[] pduPayload = new byte[pduLength - 4];
                                parser.read(pduPayload);
                                
                                
                                // delgate to pdu facade now
                                try {
                                    LogService.stackTraceLog.debug(this.smppConnection.getEsmeLabel() + " - Decoding Delgate for PDU: " + prettyPrint(pduPayload));
                                    ParsingDelegate switchingDelegator = new ParsingDelegate(pduPayload, this.smppConnection.getMode());
                                    this.processorPool.submit(switchingDelegator);
                                    LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - PDU handed over to facade in threadpool");
                                } catch (RejectedExecutionException e) {
                                    LogService.appLog.error(this.smppConnection.getEsmeLabel() + " - Unable to handover PDU into facade. [Pretty Print the PDU later] Reason: " + e.getMessage());
                                }
                            } else {
                                break; // allow the sliding window to handle the rest of the burst in the next incoming packet
                            }
                        }
                    } while (slidingWindow.canRead()); // sliding window loop
                    
                } catch (IOException e) {
                    LogService.appLog.error("Broken Pipe or Buffer!! Better to shutdown this stack", e);
                    Esme session = StackMap.getStack(this.smppConnection.getEsmeLabel());
                    LogService.appLog.debug("Session for " + this.smppConnection.getEsmeLabel() + " found: " + (session != null));
                    if (session != null) session.stop();
                    this.canRun = false;
                    break;
                } catch (SmppTransportException e) {
                    LogService.appLog.error("Sliding Window broken!! Better to shutdown this stack", e);
                    Esme session = StackMap.getStack(this.smppConnection.getEsmeLabel());
                    LogService.appLog.debug("Session for " + this.smppConnection.getEsmeLabel() + " found: " + (session != null));
                    if (session != null) session.stop();
                    this.canRun = false;
                    break;
                } // end of try block
            } // end if block (state check)
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
