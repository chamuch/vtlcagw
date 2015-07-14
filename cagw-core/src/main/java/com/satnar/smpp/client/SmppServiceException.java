package com.satnar.smpp.client;

import com.satnar.smpp.SmppStackException;

public class SmppServiceException extends SmppStackException {
    private static final long serialVersionUID = 3405696486167894094L;

    
    public SmppServiceException(String message) {
        super(message);
    }
    
    public SmppServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
