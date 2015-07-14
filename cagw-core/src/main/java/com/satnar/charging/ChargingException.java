package com.satnar.charging;

public class ChargingException extends Exception {
    private static final long serialVersionUID = -6006046576603556499L;

    public ChargingException(String message) {
        super(message);
    }
    
    public ChargingException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }
    
}
