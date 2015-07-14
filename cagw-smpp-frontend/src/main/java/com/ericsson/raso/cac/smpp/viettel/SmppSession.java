package com.ericsson.raso.cac.smpp.viettel;

import java.util.Properties;

import org.springframework.context.SmartLifecycle;

import com.satnar.common.LogService;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.client.SmppServiceException;

public class SmppSession implements ISmppEndpoint {
	
	private Esme smppSession = null;
	private State state = null;
	
	public SmppSession(Properties esmeConfig) {
		this.state = State.NOT_INIT;
		this.smppSession = new Esme(esmeConfig);
	}

	@Override
	public boolean isRunning() {
		return (this.state == State.RUNNING);
	}

	@Override
	public void start() {
		try {
			LogService.appLog.debug("SmppSession-start:Initiated...");
			this.smppSession.start();
			this.state = State.RUNNING;
		} catch (SmppServiceException e) {
            // TODO Log this to troubleshoot. putting the stack to SHUTDOWN mode...
			LogService.appLog.debug("SmppSession-start:Encounterd exception. putting the stack to SHUTDOWN mode!!",e);
            this.state = State.SHUTDOWN;
		}
	}

	@Override
	public void stop() {
		this.smppSession.stop();
	}

	@Override
	public int getPhase() {
		return 0;
	}

	
	@Override
	public boolean isAutoStartup() {
		return true;
	}

	
	@Override
	public void stop(Runnable callback) {
		this.smppSession.stop();
	}

}
