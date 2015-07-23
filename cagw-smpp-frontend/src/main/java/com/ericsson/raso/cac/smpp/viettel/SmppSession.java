package com.ericsson.raso.cac.smpp.viettel;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.SmartLifecycle;

import com.ericsson.raso.cac.config.ConfigService;
import com.satnar.common.LogService;
import com.satnar.common.SpringHelper;
import com.satnar.common.alarmlog.AlarmCode;
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
		
		LogService.alarm(AlarmCode.SYSTEM_START_UP, null);
		
		//14-Jul-2015: init from bean is behaving as singleton
		this.startStackSessions();
	}


	public void startStackSessions() {
	    try {
	        LogService.appLog.debug("SmppSession-start:Initiated...");
	        
	        String param = SpringHelper.getConfig().getValue(GLOBAL, cfgSmppSessionList);
	        if (param == null || param.equals(""))
	            throw new SmppServiceException(cfgSmppSessionList + " was not defined in " + GLOBAL + " section in the config.xml");
	        
	        String[] smppSessionList = param.split(",");
	        LogService.appLog.debug("SmppSessionList..:"+smppSessionList.length);
	        for (String smppSection: smppSessionList) {
	        	LogService.appLog.debug("SmppSession-start:Init SMPP Session with: " + smppSection);
	            Properties smppSessionProperties = SpringHelper.getConfig().getProperties(smppSection);
	            Esme smppSession = new Esme(smppSessionProperties);
	            
	            // to avoid bad network or connection issues in preventing the init logic to block forever, the smpp stack start will now happen in backend
	            new Thread(new BackgroundStackStart(smppSession)).start();
                
	            
	            LogService.appLog.debug("SmppSession-start:Async init started for SMPP Session with: " + smppSection);
	            this.smppSessions.put(smppSection, smppSession);
	        }
            LogService.appLog.error("SmppSession-start:All stacks are triggered to init!!");
	        
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


    @Override
    protected void finalize() throws Throwable {
        LogService.appLog.info("SMS Service is shutting down");
        this.stopStackSessions();
        LogService.alarm(AlarmCode.SYSTEM_SHUTDOWN, null);
        super.finalize();
    }


	
	

}
