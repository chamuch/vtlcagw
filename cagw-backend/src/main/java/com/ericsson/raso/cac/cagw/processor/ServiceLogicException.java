package com.ericsson.raso.cac.cagw.processor;

public class ServiceLogicException extends Exception {
    private static final long serialVersionUID = -8254322559495199548L;

	public ServiceLogicException(String message) {
		super(message);
	}
	
	public ServiceLogicException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
