package com.satnar.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogService {
	
	public static final String STACKTRACE = "cagw_stacktrace";
	public static final String APPLICATION = "cagw_application";
	public static final String ALARM = "cagw_alarm";
	public static Logger stackTraceLog = LoggerFactory.getLogger(STACKTRACE);
	public static Logger appLog = LoggerFactory.getLogger(APPLICATION);
	public static Logger alarm =  LoggerFactory.getLogger(ALARM);	
	
}
