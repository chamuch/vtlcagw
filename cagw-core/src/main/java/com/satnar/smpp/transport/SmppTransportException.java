package com.satnar.smpp.transport;

import com.satnar.smpp.SmppStackException;

public class SmppTransportException extends SmppStackException {
    private static final long serialVersionUID = -3728615792191563389L;

    public SmppTransportException(String message) {
        super(message);
    }
    
    public SmppTransportException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
