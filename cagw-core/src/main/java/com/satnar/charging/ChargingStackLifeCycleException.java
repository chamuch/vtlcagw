package com.satnar.charging;

public class ChargingStackLifeCycleException extends ChargingException {
    private static final long serialVersionUID = 3394563813842720316L;

    public ChargingStackLifeCycleException(String message) {
        super(message);
    }
    
    public ChargingStackLifeCycleException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
