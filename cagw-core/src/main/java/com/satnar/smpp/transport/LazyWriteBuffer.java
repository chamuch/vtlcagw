package com.satnar.smpp.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.satnar.common.LogService;

public class LazyWriteBuffer {
    
    private ByteArrayOutputStream lazyBuffer = null;
    
    public synchronized void write(byte[] payload) {
        if (payload == null || payload.length == 0) {
            LogService.appLog.debug("payload was empty/null... No action!!");
            return;
        }
        
        if (lazyBuffer == null)
            lazyBuffer = new ByteArrayOutputStream();
        
        try {
            lazyBuffer.write(payload);
            LogService.appLog.info("Buffering payload for size: " + payload.length + ", current lazy buffer size: " + this.lazyBuffer.size());
        } catch (IOException e) {
            LogService.appLog.error("Unable to buffer lazy write payload for size: " + payload.length);
        }
    }
    
    public boolean readyToTransmit() {
        if (this.lazyBuffer == null) {
            LogService.appLog.debug("Lazy Buffer has no content!! Not ready to transmit");
            return false;
        }
        
        LogService.appLog.debug("Current Lazy Buffer Size: " + this.lazyBuffer.size());
        if (this.lazyBuffer.size() > 1000) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean hasContent() {
        if (this.lazyBuffer == null) {
            LogService.appLog.debug("Lazy Buffer has no content!!");
            return false;
        }
        
        LogService.appLog.debug("Current Lazy Buffer Size: " + this.lazyBuffer.size());
        if (this.lazyBuffer.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public byte[] flush() {
        if (this.lazyBuffer == null) {
            LogService.appLog.debug("Lazy Buffer has no content!! Flushing empty payload");
            return new byte[] {};
        }
        
        byte[] flush = this.lazyBuffer.toByteArray();
        try {
            this.lazyBuffer.close();
        } catch (IOException e) {
            LogService.appLog.info("Error while cleaning up the lazy buffer!! Can be ignored!!");
        }
        this.lazyBuffer = new ByteArrayOutputStream();
        
        return flush;
    }
    
}
