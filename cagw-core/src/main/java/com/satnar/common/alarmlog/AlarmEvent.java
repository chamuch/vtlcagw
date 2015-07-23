package com.satnar.common.alarmlog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.MissingFormatArgumentException;
import java.util.Properties;

import com.satnar.common.LogService;
import com.satnar.common.SpringHelper;


public class AlarmEvent {
    
    private static final String alarmConfigSection = "GLOBAL_Alarms";
    
    private static String       managementHost     = null;
    private static String       managementPort     = null;
    private static String       communityString    = null;
    private static String       timestampFormat    = null;
    private static String       nodeName           = null;
    private static String       applicationTrapOid = null;
    private static SimpleDateFormat timestampFormatter = null;
    private AlarmCode           code               = null;
    private String              message            = null;
    private String              additionalMessage  = null;
    
    static {
        Properties configSettings = SpringHelper.getConfig().getProperties(alarmConfigSection);
        managementHost = configSettings.getProperty("managementHost");
        managementPort = configSettings.getProperty("managementPort");
        communityString = configSettings.getProperty("communityString");
        timestampFormat = configSettings.getProperty("timestampFormat");
        nodeName = configSettings.getProperty("nodeName");
        applicationTrapOid = configSettings.getProperty("applicationTrapOid");
        timestampFormatter = new SimpleDateFormat(timestampFormat);
        
    }
    
    public AlarmEvent(AlarmCode code, Object... messageParams) {
        try {
            this.code  = code;
            this.message = String.format(code.getAlarmMessage(), messageParams);
            
            if (message != null && messageParams[messageParams.length - 1] instanceof Throwable) {
                Throwable exception = (Throwable) messageParams[messageParams.length - 1];
                ByteArrayOutputStream baosStackTrace = new ByteArrayOutputStream();
                PrintWriter pwStackTrace = new PrintWriter(baosStackTrace, true);
                exception.printStackTrace(pwStackTrace);
                
                this.additionalMessage = new String(baosStackTrace.toByteArray());
                try {
                    pwStackTrace.close();
                    baosStackTrace.close();
                } catch (IOException e) {
                    // do nothing
                    e.printStackTrace();
                }
                pwStackTrace = null;
                baosStackTrace = null;
            }
        } catch (MissingFormatArgumentException e) {
            LogService.appLog.error("Insufficient arguments supplied for Alarm Event: " + code.getAlarmMessage() + 
                    ", found: " + ((messageParams != null)?messageParams.length + " items ":"null") ,e );
        } catch (Exception e) {
            LogService.appLog.error("Unforeseen problem creating alarm event. Will drop event: " + code, e);
        }
    }

    @Override
    public String toString() {
        return String.format("|timestamp=%s, alarm=%s, alarmCode=%s, alarmMode=%s, alarmType=%s, nodeLocationType=%s, severity=%s, managementHost=%s, managementPort=%s, "
                + "communityString=%s, nodeName=%s, applicationTrapOid=%s, message=%s, additionalMessage=%s", 
                timestampFormatter.format(new Date()),
                this.code, this.code.getAlarmCode(), this.code.getAlarmMode(), this.code.getAlarmType(), this.code.getNodeLocationType(), this.code.getAlarmSeverity(),
                managementHost, managementPort, communityString, nodeName, applicationTrapOid,
                message, additionalMessage);
    }
    
    
    
}
