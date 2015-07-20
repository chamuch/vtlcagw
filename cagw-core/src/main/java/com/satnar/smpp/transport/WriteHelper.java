package com.satnar.smpp.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import com.satnar.common.LogService;
import com.satnar.smpp.StackMap;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.pdu.SmppPdu;

public class WriteHelper {
    
    private int lazyWritePeriod = 0;
    private Timer lazyWriteSchedule = null;
    private LazyWriteBuffer lazyWriteBuffer = null;
    
    private Connection smppConnection = null;
    
    public WriteHelper(Connection connection) {
        this.smppConnection = connection;
        this.lazyWriteBuffer = new LazyWriteBuffer();
        lazyWritePeriod = this.smppConnection.getLazyWriteWait();
        this.lazyWriteSchedule = new Timer("LazyWriter-" + this.smppConnection.getEsmeLabel());
        LogService.appLog.info("Lazy Writer Scheduled with Frequency: " + this.lazyWritePeriod);
        this.lazyWriteSchedule.schedule(new LazyWriterTask(this.smppConnection, this.lazyWriteBuffer), this.lazyWritePeriod, this.lazyWritePeriod);
    }
    
    public void writeImmediate(SmppPdu payload) throws SmppCodecException, SmppTransportException  {
        if (this.smppConnection.getConnectionState() == SmppSessionState.CLOSED ||
                this.smppConnection.getConnectionState() == SmppSessionState.UNBOUND)
            throw new SmppTransportException(this.smppConnection.getEsmeLabel() + " - SMPP Session closed or Socket is broken. Reinitialize ESME now!!");
        
        try {
            ByteBuffer writeBuffer = this.smppConnection.getRequestBuffer();
            byte[] serialized = payload.encode();
            LogService.stackTraceLog.debug(this.smppConnection.getEsmeLabel() + " - transmitting payload: " + prettyPrint(serialized));
            synchronized (writeBuffer) {
                writeBuffer.clear();
                writeBuffer.put(serialized);
                writeBuffer.flip();
                this.smppConnection.write(writeBuffer);
                writeBuffer.clear();
            }
            
            LogService.stackTraceLog.info(this.smppConnection.getEsmeLabel() + " - WriteHelper-writeImmediate:Done. Command Id:"+payload.getCommandId().name()+":Command Sequence:"+payload.getCommandSequence().getValue());
        }  catch (SmppTransportException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                LogService.stackTraceLog.debug(this.smppConnection.getEsmeLabel() + " - WriteHelper-writeImmediate:socket seems to be broken. Command Id:"+payload.getCommandId().name()+":Command Sequence:"+payload.getCommandSequence().getValue(),e);
                Esme session = StackMap.getStack(this.smppConnection.getEsmeLabel());
                if (!((TcpConnection)this.smppConnection).isShutdownMode())
                    session.stop();
            }
            
        }
    }
    
    private String prettyPrint(byte[] serialized) {
        StringBuilder sbPrettyPrint = new StringBuilder();
        for (byte atom: serialized) {
            sbPrettyPrint.append(Integer.toHexString((0xff&atom)));
            sbPrettyPrint.append(" ");
        }
        return sbPrettyPrint.toString();
    }

    public void writeLazy(SmppPdu payload) throws SmppCodecException, SmppTransportException  {
        if (this.smppConnection.getConnectionState() == SmppSessionState.CLOSED ||
                this.smppConnection.getConnectionState() == SmppSessionState.UNBOUND)
            throw new SmppTransportException(this.smppConnection.getEsmeLabel() + " - SMPP Session closed or Socket is broken. Reinitialize ESME now!!");
        
        try {
            byte[] serialized = payload.encode();
            LogService.stackTraceLog.debug(this.smppConnection.getEsmeLabel() + " - buffering payload: " + prettyPrint(serialized));
            this.lazyWriteBuffer.write(serialized);
            
            
            if (this.lazyWriteBuffer.readyToTransmit()) {
                ByteBuffer writeBuffer = this.smppConnection.getRequestBuffer();
                synchronized (writeBuffer) {
                    writeBuffer.clear();
                    writeBuffer.put(this.lazyWriteBuffer.flush());
                    writeBuffer.flip();
                    this.smppConnection.write(writeBuffer);
                    writeBuffer.clear();
                    LogService.stackTraceLog.debug(this.smppConnection.getEsmeLabel() + " - flushed transmission window");
                }
            }
            
            LogService.stackTraceLog.info(this.smppConnection.getEsmeLabel() + " - WriteHelper-writeLazy:Done. Command Id:"+payload.getCommandId().name()+":Command Sequence:"+payload.getCommandSequence().getValue());
        }  catch (SmppTransportException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                //TODO: Log - socket seems to be broken.
            	LogService.stackTraceLog.debug(this.smppConnection.getEsmeLabel() + " - WriteHelper-writeLazy:socket seems to be broken. Command Id:"+payload.getCommandId().name()+":Command Sequence:"+payload.getCommandSequence().getValue(),e);
                Esme session = StackMap.getStack(this.smppConnection.getEsmeLabel());
                session.stop();
            }

        }
    }
    

    public void stop() {
        this.lazyWriteSchedule.cancel();
        this.lazyWriteSchedule = null;
        
    }

    
    


    
    class LazyWriterTask extends TimerTask {
        
        private Connection connection = null;
        private LazyWriteBuffer lazyWriteBuffer = null;
        
        public LazyWriterTask(Connection connection, LazyWriteBuffer lazyWriteBuffer) {
            this.connection = connection;
            this.lazyWriteBuffer = lazyWriteBuffer;
        }

        @Override
        public void run() {
            try {
                if (this.connection.getConnectionState() == SmppSessionState.BOUND_RX ||
                        this.connection.getConnectionState() == SmppSessionState.BOUND_TX ||
                        this.connection.getConnectionState() == SmppSessionState.BOUND_TRX) {
                    
                    LogService.appLog.debug("LazyWriter - connection state:" + this.connection.getConnectionState() + ", valid for write!!");
                    if (this.lazyWriteBuffer.hasContent()) {
                        ByteBuffer writeBuffer = this.connection.getRequestBuffer();
                        synchronized (writeBuffer) {
                            writeBuffer.clear();
                            writeBuffer.put(this.lazyWriteBuffer.flush());
                            writeBuffer.flip();
                            this.connection.write(writeBuffer);
                            writeBuffer.clear();
                            LogService.stackTraceLog.debug(this.connection.getEsmeLabel() + " - flushed transmission window");
                        }
                    } else {
                        LogService.appLog.debug("LazyWriter - nothing to flush");
                    }
                }
                
            } catch (SmppTransportException e) {
                if (e.getCause() != null && e.getCause() instanceof IOException) {
                    //TODO: Log - socket seems to be broken.
                	LogService.stackTraceLog.debug("WriteHelper-LazyWriterTask-run:socket seems to be broken.");
                    Esme session = StackMap.getStack(this.connection.getEsmeLabel());
                    session.stop();
                }
            }
        }
    }






}
