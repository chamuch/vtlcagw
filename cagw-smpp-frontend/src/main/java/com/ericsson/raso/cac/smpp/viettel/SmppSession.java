package com.ericsson.raso.cac.smpp.viettel;

import java.util.Properties;

import org.springframework.context.SmartLifecycle;

import com.satnar.common.LogService;
import com.satnar.common.SpringHelper;
import com.satnar.common.alarmlog.AlarmCode;
import com.satnar.smpp.StackMap;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.client.EsmeHelper;
import com.satnar.smpp.client.SmppServiceException;

public class SmppSession implements SmartLifecycle {
    
    private static final String cfgSmppSessionList = "smppSectionsList";
    private static final String GLOBAL = "GLOBAL";
    
	//private Esme smppSession = null;
    private String[] smppSessions = null;
	private State state = null;
    private SessionWatchdog sessionWatchdog = null;
	
	public SmppSession(Properties esmeConfig) {
		LogService.appLog.debug("SmppSession:Constructor changed...");
		this.state = State.NOT_INIT;
		
		LogService.alarm(AlarmCode.SYSTEM_START_UP, (Object)null);
		
		//14-Jul-2015: init from bean is behaving as singleton
		//this.startStackSessions();
	}


	public void startStackSessions() {
	    try {
	        LogService.appLog.debug("SmppSession-start:Initiated...");
	        
	        String param = SpringHelper.getConfig().getValue(GLOBAL, cfgSmppSessionList);
	        if (param == null || param.equals(""))
	            throw new SmppServiceException(cfgSmppSessionList + " was not defined in " + GLOBAL + " section in the config.xml");
	        
	        this.smppSessions = param.split(",");
	        LogService.appLog.debug("SmppSessionList..:" + smppSessions.length);
	        for (String smppSection: smppSessions) {
	        	LogService.appLog.debug("SmppSession-start:Init SMPP Session with: " + smppSection);
	            Properties smppSessionProperties = SpringHelper.getConfig().getProperties(smppSection);
	            if (smppSessionProperties == null) {
	                LogService.appLog.error("Found ESME Session: " + smppSection + " but is not configured!");
	                break;
	            }
	            Esme smppSession = new Esme(smppSessionProperties);
	            
	            // to avoid bad network or connection issues in preventing the init logic to block forever, the smpp stack start will now happen in backend
	            new Thread(new BackgroundStackStart(smppSession)).start();
                
	            
	            LogService.appLog.debug("SmppSession-start:Async init started for SMPP Session with: " + smppSection);
	        }
	        LogService.appLog.info("SmppSession-start:All stacks are triggered to init!!");
            
	        this.sessionWatchdog = new SessionWatchdog(smppSessions);
	        LogService.appLog.info("SmppSession-start:Watchdog started!!");
            
            
	        this.state = State.RUNNING;
	    } catch (SmppServiceException e) {
	        // TODO Log this to troubleshoot. putting the stack to SHUTDOWN mode...
	        LogService.appLog.error("SmppSession-start:Encounterd exception. putting the stack to SHUTDOWN mode!!",e);
	        this.state = State.SHUTDOWN;
	    }
	}

	public void stopStackSessions() {
	    for (String smppSession: this.smppSessions) {
	        Esme session = StackMap.getStack(smppSession);
	        if (session != null && EsmeHelper.checkSessionState(smppSession))
	            session.stop();
	    }
	    LogService.appLog.info("SmppSession-stop: All stacks are stopped.");
	}


    @Override
    protected void finalize() throws Throwable {
        LogService.appLog.info("SMS Service is shutting down");
        this.stopStackSessions();
        LogService.alarm(AlarmCode.SYSTEM_SHUTDOWN, (Object)null);
        super.finalize();
    }


    @Override
    public boolean isRunning() {
        return (this.state == State.RUNNING);
    }


    @Override
    public void start() {
        this.startStackSessions();
        
    }


    @Override
    public void stop() {
        this.stopStackSessions();
    }


    @Override
    public int getPhase() {
        return 1;
    }


    @Override
    public boolean isAutoStartup() {
        return true;
    }


    @Override
    public void stop(Runnable arg0) {
        this.stopStackSessions();
    }


	
	

}
