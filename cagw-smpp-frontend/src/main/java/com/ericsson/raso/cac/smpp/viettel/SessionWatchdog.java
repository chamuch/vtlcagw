package com.ericsson.raso.cac.smpp.viettel;

import java.util.Timer;
import java.util.TimerTask;

import com.satnar.common.LogService;
import com.satnar.common.SpringHelper;
import com.satnar.smpp.client.EsmeHelper;

public class SessionWatchdog {

    private static final String GLOBAL = "GLOBAL";
    private static final String MONITOR_INTERVAL = "monitorInterval";
    private static final String MONITOR_INITIAL_WAIT = "monitorInitialWait";
    
    private String[] smppSessions = null;
    private int monitorInteval = 0;
    private int monitorInitialWait = 0;
    private Timer watchdogSchedule = null;
    
    public SessionWatchdog(String[] smppSessionList) {
        this.smppSessions = smppSessionList;
    }

    public void start() {
        String param = null;
        try {
            param = SpringHelper.getConfig().getValue(GLOBAL, MONITOR_INITIAL_WAIT);
            if (param == null) {
                LogService.appLog.error("Watchdog not started. Configuration in Section: " + GLOBAL + ", Property: " + MONITOR_INITIAL_WAIT + " was not configured!");
                return;
            }
            this.monitorInitialWait = Integer.parseInt(param);
        } catch (NumberFormatException e) {
            LogService.appLog.error("Watchdog not started. Configuration in Section: " + GLOBAL + ", Property: " + MONITOR_INITIAL_WAIT + " was configured with invalid value. Expected Integer!");
            return;
        }
        
        try {
            param = SpringHelper.getConfig().getValue(GLOBAL, MONITOR_INTERVAL);
            if (param == null) {
                LogService.appLog.error("Watchdog not started. Configuration in Section: " + GLOBAL + ", Property: " + MONITOR_INTERVAL + " was not configured!");
                return;
            }
            this.monitorInteval = Integer.parseInt(param);
        } catch (NumberFormatException e) {
            LogService.appLog.error("Watchdog not started. Configuration in Section: " + GLOBAL + ", Property: " + MONITOR_INTERVAL + " was configured with invalid value. Expected Integer!");
            return;
        }
        
        
        this.watchdogSchedule = new Timer("SmppSessionsWatchdog");
        this.watchdogSchedule.schedule(new SessionWatchdogTask(this.smppSessions), this.monitorInitialWait, this.monitorInteval);
        LogService.appLog.info("Watchdog successfully started!!");
    }
    
    public void stop() {
        this.watchdogSchedule.cancel();
    }
    
    
    class SessionWatchdogTask extends TimerTask {
        private String[] smppSessions = null;
        
        SessionWatchdogTask(String[] sessions) {
            this.smppSessions = sessions;
        }

        @Override
        public void run() {
            LogService.appLog.info("Watchdog monitor cycle initiated...");
            for (String smppSession: this.smppSessions) {
                if (!EsmeHelper.checkSessionState(smppSession)) {
                    LogService.appLog.warn("SmppSession: " + smppSession + " was detected invalid state. Attempting (re)start...");
                    new Thread(new WatchdogSessionStart(smppSession)).start();
                }
            }
            LogService.appLog.info("Watchdog monitor cycle complete!");
        }
        
        
    }
    
}
