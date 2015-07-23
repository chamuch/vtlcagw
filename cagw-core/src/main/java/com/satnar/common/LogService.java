package com.satnar.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.satnar.common.alarmlog.AlarmCode;
import com.satnar.common.alarmlog.AlarmEvent;

public class LogService {
	
	public static final String STACKTRACE = "cagw_stacktrace";
	public static final String APPLICATION = "cagw_application";
	public static final String ALARM = "cagw_alarm";
	public static Logger stackTraceLog = LoggerFactory.getLogger(STACKTRACE);
	public static Logger appLog = LoggerFactory.getLogger(APPLICATION);
	public static Logger alarm =  LoggerFactory.getLogger(ALARM);	
	
	public static void alarm(AlarmCode code, Object... messageParams){
	    AlarmEvent event = new AlarmEvent(code, messageParams);
	    alarm.error(event.toString());
	}
	
}
