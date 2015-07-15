package com.satnar.smpp.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.satnar.common.LogService;

public class SlidingWindowBuffer {
    
    private byte[] slidingWindow = null;
    private ByteArrayOutputStream incoming = null;
    private ByteArrayInputStream consuming = null;
    
    public synchronized void push(byte[] payload) throws SmppTransportException {
        try {
            // check for previous sliding window remaining...
            if (this.consuming != null) {
                int burstWindow = this.consuming.available();
                LogService.appLog.debug("Previous remaining window size: " + burstWindow);
                if (burstWindow > 0) {
                    this.slidingWindow = new byte[burstWindow];
                    this.consuming.read(this.slidingWindow);
                } else {
                    this.slidingWindow = null;
                }
            }
            
            
            this.incoming = new ByteArrayOutputStream();
            if (slidingWindow != null && slidingWindow.length > 0) {
                LogService.appLog.debug("Existing Sliding Window: " + this.slidingWindow.length + ", Incoming Window: " + payload.length);
                this.incoming.write(this.slidingWindow);                
                this.incoming.write(payload);
            } else {
                LogService.appLog.debug("No Existing Sliding Window: 0, Incoming Window: " + payload.length);
                this.incoming.write(payload);
            }
            byte[] consolidatedWindow = this.incoming.toByteArray();
            this.incoming.close();
            this.incoming = null;
            
            this.consuming = new ByteArrayInputStream(consolidatedWindow);
            
            
        } catch (IOException e) {
            throw new SmppTransportException("Cannot accept incoming stream. Size: " + payload.length);
        }
    }
    
    public InputStream getConsumingStream() {
        return this.consuming;
    }

    public boolean canRead() {
        if (this.consuming == null)
            return false;
        return (this.consuming.available() > 0);
    }
    
}
