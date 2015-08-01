package com.satnar.smpp.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.satnar.common.LogService;
import com.satnar.common.alarmlog.AlarmCode;
import com.satnar.smpp.StackMap;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;
import com.satnar.smpp.pdu.ParsingDelegate;

public class ReadHelper implements Runnable { 
    
    private boolean         canRun         = true;
    private Connection      smppConnection = null;
    private int             threadPoolSize = 0;
    private ExecutorService processorPool  = null;   
    
    
    public ReadHelper(Connection connection) {
        this.smppConnection = connection;
        this.threadPoolSize = this.smppConnection.getThreadPoolSize();
        
        this.processorPool = new ThreadPoolExecutor((this.threadPoolSize),    // initial pool size
                this.threadPoolSize,                                            // max pool size
                300,                                                          // keep alive time
                TimeUnit.MILLISECONDS,                                          // keep alive time unit (set to 30 secs)
                new LinkedBlockingQueue<Runnable>((this.threadPoolSize)));    // wait queue size
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
                    LogService.appLog.debug("Session for " + this.smppConnection.getEsmeLabel() + " found: " + (session != null));
                    if (session != null) session.stop();
                    this.canRun = false;
                    break;
                }
                
                ByteBuffer readBuffer = this.smppConnection.getResponseBuffer();
                
                int windowSize = 0;
                try {
                    byte[] currentWindow = null;
                    synchronized (readBuffer) {
                        windowSize =  this.smppConnection.read(readBuffer);
                        LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Incoming Packet Size: " + windowSize);
                        // check the incoming packet size...
                        if (windowSize > 0) {
                            currentWindow = new byte[windowSize];
                            readBuffer.get(currentWindow); readBuffer.clear();
                            LogService.stackTraceLog.info(this.smppConnection.getEsmeLabel() + " - Read (" + windowSize + ") bytes with payload: " + prettyPrint(currentWindow));
                            
                        } else { // else, lets wait 0.5 sec (this can be optimized later)
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                // do nothing
                            } // end of catch
                        } // end of window size
                    } // end of sync block on read buffer
                    
                    // process only if we read anything from the socket..
                    if (windowSize > 0) {
                        // adjust the sliding window
                        LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Sliding window adjusted with incoming window size: " + windowSize);
                        slidingWindow.push(currentWindow);
                        DataInputStream parser = slidingWindow.getParser();
                        LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Sliding window new size: " + parser.available());
                        
                        // process the burst...
                        do {
                            int currentSlidingReminaing = parser.available();
                            LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - Sliding window current remaining size: " + currentSlidingReminaing);
                            if (currentSlidingReminaing > 4) { // to ensure we have enough bytes to atleast decode PDU length...
                                int pduLength = parser.readInt();
                                currentSlidingReminaing = parser.available();
                                LogService.appLog.debug("Next PDU to process needs (" + pduLength + ") bytes & sliding window curr_size: " + currentSlidingReminaing);
                                if ((pduLength>0) && (pduLength - 4) <= currentSlidingReminaing ) {
                                    LogService.appLog.debug("Sliding Window has (" + pduLength + ") bytes to read & process");
                                    
                                    byte[] pduPayload = new byte[pduLength - 4];
                                    parser.read(pduPayload);
                                    
                                    
                                    // delgate to pdu facade now
                                    try {
                                        LogService.stackTraceLog.info(this.smppConnection.getEsmeLabel() + " - Decoding Delgate for PDU: " + prettyPrint(pduPayload));
                                        ParsingDelegate switchingDelegator = new ParsingDelegate(pduPayload, this.smppConnection.getEsmeLabel(), this.smppConnection.getMode());
                                        this.processorPool.submit(switchingDelegator);
                                        LogService.alarm(AlarmCode.SMS_CONGESTTION_ABATE, this.smppConnection.getEsmeLabel());
                                        LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - PDU handed over to facade in threadpool");
                                    } catch (RejectedExecutionException e) {
                                        LogService.appLog.error(this.smppConnection.getEsmeLabel() + " - Unable to handover PDU into facade. " + prettyPrint(pduPayload) + ", Reason: ", e);
                                        int commandId = parser.readInt();
                                        parser.readInt(); // skip the status
                                        int sequence = parser.readInt();
                                        LogService.alarm(AlarmCode.SMS_CONGESTTION_DROP, this.smppConnection.getEsmeLabel(), commandId, sequence);
                                        LogService.appLog.error(this.smppConnection.getEsmeLabel() + " - Threadpool congested. Cannot handle PDU with commandId: " + commandId + " & sequence: " + sequence);
                                    }
                                } else {
                                    LogService.appLog.debug(this.smppConnection.getEsmeLabel() + " - allow the sliding window to handle the rest of the burst in the next incoming packet");
                                    slidingWindow.rewind(SmppParameter.getInstance(Type.INTEGER, pduLength).encode());
                                    break; // allow the sliding window to handle the rest of the burst in the next incoming packet
                                }
                            }
                        } while (slidingWindow.canRead()); // sliding window loop
                        
                    } // end of if block - processing only if we have data to process
                    
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
            sbPrettyPrint.append(Integer.toHexString( (0xff&atom)));
            sbPrettyPrint.append(" ");
        }
        return sbPrettyPrint.toString();
    }
    
    
}
