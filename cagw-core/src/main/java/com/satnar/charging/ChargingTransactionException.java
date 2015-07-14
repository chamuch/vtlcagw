package com.satnar.charging;

public class ChargingTransactionException extends ChargingException {
    private static final long serialVersionUID = 8317719274511913333L;

    public ChargingTransactionException(String message) {
        super(message);
    }
    
    public ChargingTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
