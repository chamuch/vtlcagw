package com.satnar.smpp;

public class SmppStackException extends Exception {
    private static final long serialVersionUID = -6229231454348622724L;

    public SmppStackException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmppStackException(String message) {
        super(message);
    }

    
}
