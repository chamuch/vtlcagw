package com.ericsson.raso.cac.cagw.dao;

public class PersistenceException extends Exception {
    private static final long serialVersionUID = 4522041460654857235L;

    public PersistenceException(String message) {
        super(message);
    }
    
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
