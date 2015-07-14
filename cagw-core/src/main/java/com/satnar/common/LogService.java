package com.satnar.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogService {
	
	public static final String STACKTRACE = "cagw_stacktrace";
	public static final String APPLICATION = "cagw_application";
	public static Logger stackTraceLog = LoggerFactory.getLogger(STACKTRACE);
	public static Logger appLog = LoggerFactory.getLogger(APPLICATION);
		
	/*public static <T> void debug(Class<T> T,String message){
		stackTraceLog.debug(T.getName(),message);
	}
	
	public static <T> void info(Class<T> T,String message){
		appLog.info(T.getName(),message);
	}
	*/
}
