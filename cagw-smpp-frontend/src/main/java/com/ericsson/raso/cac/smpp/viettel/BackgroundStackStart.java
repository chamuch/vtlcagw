package com.ericsson.raso.cac.smpp.viettel;

import com.satnar.common.LogService;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.client.SmppServiceException;

public class BackgroundStackStart implements Runnable {
    
    private Esme session = null;
    private String esmeLabel = null;
    
    public BackgroundStackStart(Esme session, String esmelabel) {
        this.session = session;
        this.esmeLabel = esmelabel;
    }

    @Override
    public void run() {
        LogService.appLog.debug("Attempting to start smpp session in async mode. Stack: " + esmeLabel);
        try {
            session.start();
            LogService.appLog.debug("Successful start smpp session in async mode. Stack: " + esmeLabel);
        } catch (SmppServiceException e) {
            LogService.appLog.error("Session failed to start. Stack: " + esmeLabel, e);
        }
        
    }
    
}
