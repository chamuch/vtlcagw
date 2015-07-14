package com.ericsson.raso.cac.smpp.viettel;

import com.satnar.common.LogService;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.client.SmppServiceException;

public class BackgroundStackStart implements Runnable {
    
    private Esme session = null;
    
    public BackgroundStackStart(Esme session) {
        this.session = session;
    }

    @Override
    public void run() {
        LogService.appLog.debug("Attempting to start smpp session in async mode. Stack: " + session.getUsername() + "@" + session.getSystemType());
        try {
            session.start();
            LogService.appLog.debug("Successful start smpp session in async mode. Stack: " + session.getUsername() + "@" + session.getSystemType());
        } catch (SmppServiceException e) {
            LogService.appLog.error("Session failed to start. Stack: " + session.getUsername() + "@" + session.getSystemType());
        }
        
    }
    
}
