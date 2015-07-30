package com.satnar.smpp.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.satnar.common.LogService;

public class SlidingWindowBuffer {
    
    private byte[] slidingWindow = null;
    private ByteArrayOutputStream incoming = null;
    private ByteArrayInputStream consuming = null;
    private DataInputStream parser = null;
    
    public synchronized void push(byte[] payload) throws SmppTransportException {
        try {
            // check for previous sliding window remaining...
            if (parser != null) {
                int burstWindow = this.parser.available();
                LogService.appLog.debug("Previous remaining window size: " + burstWindow);
                if (burstWindow > 0) {
                    this.slidingWindow = new byte[burstWindow];
                    this.parser.read(this.slidingWindow);
                } else {
                    this.slidingWindow = null;
                }
            }
            this.parser.close();
            this.consuming.close();
            this.parser = null;
            this.consuming = null;
            
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
            this.parser = new DataInputStream(this.consuming);
            
            
        } catch (IOException e) {
            throw new SmppTransportException("Cannot accept incoming stream. Size: " + payload.length);
        }
    }
    
    public DataInputStream getParser() {
        return this.parser;
    }

    public boolean canRead() throws IOException {
        LogService.appLog.debug("Check parser availablity: " + (this.parser != null));
        if (this.parser == null)
            return false;
        
        int parserReadable = this.parser.available();
        LogService.appLog.debug("Sliding Window can still read: " + parserReadable );
        return (parserReadable > 0);
    }
    
}
