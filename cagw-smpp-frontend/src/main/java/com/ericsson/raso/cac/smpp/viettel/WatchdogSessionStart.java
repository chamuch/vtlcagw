package com.ericsson.raso.cac.smpp.viettel;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.satnar.common.LogService;
import com.satnar.common.SpringHelper;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.client.EsmeHelper;
import com.satnar.smpp.client.SmppServiceException;

public class WatchdogSessionStart implements Runnable {
    
    private static List<String> inProgress = new ArrayList<String>();
    
    private String esmeLabel = null;
    
    public WatchdogSessionStart(String sessionLabel) {
        this.esmeLabel = sessionLabel;
    }

    @Override
    public void run() {
        if (inProgress.contains(this.esmeLabel)) {
            LogService.appLog.warn("Seems like another Watchdog is still working on this esme: " + this.esmeLabel);
            return;
        }
        
        LogService.appLog.info("Attempting to start smpp session in async mode. Stack: " + esmeLabel);
        inProgress.add(esmeLabel);
        try {
            Properties smppSessionProperties = SpringHelper.getConfig().getProperties(esmeLabel);
            if (smppSessionProperties == null) {
                LogService.appLog.error("Found ESME Session: " + esmeLabel + " but is not configured!");
                return;
            }
            Esme session = new Esme(smppSessionProperties);
            LogService.appLog.info("Attempting to start smpp session in async mode. Stack: " + esmeLabel);
            session.start();
            if (EsmeHelper.checkSessionState(smppSessionProperties.getProperty("rx.esmeLabel"))) {
                LogService.appLog.debug("Watchdog successfully restarted session: " + esmeLabel);
            } else {
                LogService.appLog.warn("Watchdog failed attempting restart session: " + esmeLabel);
            }
        } catch (SmppServiceException e) {
            LogService.appLog.error("Watchdog failed to start session: " + esmeLabel);
        } finally {
            inProgress.remove(this.esmeLabel);
        }
        
    }

    
    
}
