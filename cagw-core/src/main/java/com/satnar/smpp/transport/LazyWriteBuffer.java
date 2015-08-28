package com.satnar.smpp.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.satnar.common.LogService;

public class LazyWriteBuffer { 
    
    private String esmeLabel = null;
    private ByteArrayOutputStream lazyBuffer = null;
    
    public LazyWriteBuffer(String esmeLabel) {
        this.esmeLabel = esmeLabel;
    }
    
    public synchronized void write(byte[] payload) {
        if (payload == null || payload.length == 0) {
            LogService.appLog.debug(String.format("Session: %s - payload was empty/null... No action!!", this.esmeLabel));
            return;
        }
        
        if (lazyBuffer == null)
            lazyBuffer = new ByteArrayOutputStream();
        
        try {
            lazyBuffer.write(payload);
            LogService.appLog.info(String.format("Session: %s - Buffering payload for size: %s, current lazy buffer size: %s", this.esmeLabel, payload.length, this.lazyBuffer.size()));
        } catch (IOException e) {
            LogService.appLog.error(String.format("Session: %s - Unable to buffer lazy write payload for size: %s", this.esmeLabel, payload.length));
        }
    }
    
    public boolean readyToTransmit() {
        if (this.lazyBuffer == null) {
            if (LogService.appLog.isDebugEnabled())
                LogService.appLog.debug(String.format("Session: %s - Lazy Buffer has no content!! Not ready to transmit", this.esmeLabel));
            return false;
        }
        
        if (this.lazyBuffer.size() > 1000) {
            if (LogService.appLog.isDebugEnabled())
                LogService.appLog.debug(String.format("Session: %s - Current Lazy Buffer Size: %s, Ready for transmission!", this.esmeLabel, this.lazyBuffer.size()));
            return true;
        } else {
            if (LogService.appLog.isDebugEnabled())
                LogService.appLog.debug(String.format("Session: %s - Current Lazy Buffer Size: %s, Not Ready for transmission!", this.esmeLabel, this.lazyBuffer.size()));
            return false;
        }
    }
    
    public boolean hasContent() {
        if (this.lazyBuffer == null) {
            if (LogService.appLog.isDebugEnabled())
                LogService.appLog.debug(String.format("Session: %s - Lazy Buffer has no content!!", this.esmeLabel));
            return false;
        }
        
        if (this.lazyBuffer.size() > 0) {
            if (LogService.appLog.isDebugEnabled())
                LogService.appLog.debug(String.format("Session: %s - Current Lazy Buffer Size: %s, Ready for transmission!", this.esmeLabel, this.lazyBuffer.size()));
            return true;
        } else {
            if (LogService.appLog.isDebugEnabled())
                LogService.appLog.debug(String.format("Session: %s - Current Lazy Buffer Size: %s, Not Ready for transmission!", this.esmeLabel, this.lazyBuffer.size()));
            return false;
        }
    }
    
    public boolean willOverflow(int payloadSize) {
        if (this.lazyBuffer == null) {
            if (LogService.appLog.isDebugEnabled())
                LogService.appLog.debug(String.format("Session: %s - Lazy Buffer has no content!!", this.esmeLabel));
            return false;
        }
        
        int newSize = this.lazyBuffer.size() + payloadSize;
        if (LogService.appLog.isInfoEnabled())
            LogService.appLog.info(String.format("Session: %s - Lazy Buffer: %s, new_payload: %s, total: %s, decision: %s", 
                    this.esmeLabel, this.lazyBuffer.size(), payloadSize, newSize, (newSize >= 1024)));
        
        if (newSize >= 1024) { //TODO: remove this hard code in final project.
            return true;
        } else {
            return false;
        }
    }
    
    public byte[] flush() {
        if (this.lazyBuffer == null) {
            if (LogService.appLog.isDebugEnabled())
                LogService.appLog.debug("Lazy Buffer has no content!! Flushing empty payload");
            return new byte[] {};
        }
        
        byte[] flush = this.lazyBuffer.toByteArray();
        try {
            this.lazyBuffer.close();
        } catch (IOException e) {
            LogService.appLog.warn("Error while cleaning up the lazy buffer!! Can be ignored!!");
        }
        this.lazyBuffer = new ByteArrayOutputStream();
        
        return flush;
    }
    
}
