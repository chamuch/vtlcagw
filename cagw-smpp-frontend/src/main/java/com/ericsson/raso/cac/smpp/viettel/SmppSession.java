package com.ericsson.raso.cac.smpp.viettel;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.SmartLifecycle;

import com.ericsson.raso.cac.config.ConfigService;
import com.satnar.common.LogService;
import com.satnar.common.SpringHelper;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.client.SmppServiceException;

public class SmppSession {
    
    private static final String cfgSmppSessionList = "smppSectionsList";
    private static final String GLOBAL = "GLOBAL";
    
	//private Esme smppSession = null;
    private Map<String, Esme> smppSessions = new HashMap<String, Esme>();
	private State state = null;
	
	public SmppSession(Properties esmeConfig) {
		LogService.appLog.debug("SmppSession:Constructor changed...");
		this.state = State.NOT_INIT;
		
		//14-Jul-2015: init from bean is behaving as singleton
		this.startStackSessions();
	}

//	public boolean isRunning() {
//		return (this.state == State.RUNNING);
//	}

	public void startStackSessions() {
	    try {
	        LogService.appLog.debug("SmppSession-start:Initiated...");
	        
	        String param = SpringHelper.getConfig().getValue(GLOBAL, cfgSmppSessionList);
	        if (param == null || param.equals(""))
	            throw new SmppServiceException(cfgSmppSessionList + " was not defined in " + GLOBAL + " section in the config.xml");
	        
	        String[] smppSessionList = param.split(",");
	        for (String smppSection: smppSessionList) {
	            Properties smppSessionProperties = SpringHelper.getConfig().getProperties(smppSection);
	            Esme smppSession = new Esme(smppSessionProperties);
	            smppSession.start();
	            this.smppSessions.put(smppSection, smppSession);
	        }
            LogService.appLog.error("SmppSession-start:All stacks are up and running!!");
	        
	        this.state = State.RUNNING;
	    } catch (SmppServiceException e) {
	        // TODO Log this to troubleshoot. putting the stack to SHUTDOWN mode...
	        LogService.appLog.error("SmppSession-start:Encounterd exception. putting the stack to SHUTDOWN mode!!",e);
	        this.state = State.SHUTDOWN;
	    }
	}

	public void stopStackSessions() {
	    for (Esme smppSession: this.smppSessions.values())
	        smppSession.stop();
	    LogService.appLog.info("SmppSession-stop: All stacks are stopped.");
	}


//	public int getPhase() {
//		return 0;
//	}
//
//
//	public boolean isAutoStartup() {
//		return true;
//	}
//
//
//	public void stop(Runnable callback) {
//		this.smppSession.stop();
//	}

}
