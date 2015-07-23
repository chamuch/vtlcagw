package com.satnar.common.alarmlog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import com.satnar.common.SpringHelper;


public class AlarmEvent {
    
    private static final String alarmConfigSection = "GLOBAL_Alarms";
    
    private static String managementHost     = null;
    private static String managementPort     = null;
    private static String communityString    = null;
    private static String timestampFormat    = null;
    private static String nodeName           = null;
    private static String applicationTrapOid = null;
    private String        message            = null;
    private String        additionalMessage  = null;
    
    static {
        Properties configSettings = SpringHelper.getConfig().getProperties(alarmConfigSection);
        managementHost = configSettings.getProperty("managementHost");
        managementPort = configSettings.getProperty("managementPort");
        communityString = configSettings.getProperty("communityString");
        timestampFormat = configSettings.getProperty("timestampFormat");
        nodeName = configSettings.getProperty("nodeName");
        applicationTrapOid = configSettings.getProperty("applicationTrapOid");
        
    }
    
    public AlarmEvent(AlarmCode code, Object... messageParams) {
        this.message = String.format(code.getAlarmMessage(), messageParams);
        
        if (messageParams[messageParams.length - 1] instanceof Throwable) {
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
    }
    
}
