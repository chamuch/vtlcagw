package com.satnar.air.ucip.client;

public class UcipException extends Exception {
    private static final long serialVersionUID = 1309670376651913794L;
    
    private int code = 0;
    
    public UcipException(String message) {
        super(message);
    }
    
    public UcipException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    
    public UcipException(String message, Throwable cause) {
        super(message, cause);
    }

    public UcipException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    
    
    
}
