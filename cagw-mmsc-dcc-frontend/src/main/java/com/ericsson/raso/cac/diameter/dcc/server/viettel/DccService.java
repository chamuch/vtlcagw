package com.ericsson.raso.cac.diameter.dcc.server.viettel;

import java.util.Properties;

import org.springframework.context.SmartLifecycle;

import com.ericsson.pps.diameter.rfcapi.base.PeerConnectionListener;
import com.ericsson.pps.diameter.rfcapi.base.message.ApplicationRequestListener;
import com.satnar.charging.diameter.dcc.server.DccServiceEndpoint;
import com.satnar.charging.diameter.dcc.server.DiameterServiceEndpoint;
import com.satnar.common.LogService;
import com.satnar.common.alarmlog.AlarmCode;
import com.satnar.common.charging.ChargingStackLifeCycleException;

public class DccService implements SmartLifecycle {
    
    private Properties config = null;
    private DiameterServiceEndpoint dccServiceEndpoint = null;
    private ApplicationRequestListener requestListener = null;
    private PeerConnectionListener peerConnectionListener = null;
    private State state = null;
    
    public DccService(Properties serviceConfig) {    	
        this.config = serviceConfig;
        this.state = State.NOT_INIT;        
    }
    

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void start() {    	
        this.state = State.NOT_INIT;
        this.dccServiceEndpoint = SpringHelper.getDiameterStack();
        ((DccServiceEndpoint)this.dccServiceEndpoint).setConfig(this.config);
        
        /*this.peerConnectionListener = SpringHelper.getPeerConnectionListener();
        this.dccServiceEndpoint.setPeerConnectionListener(this.peerConnectionListener);*/
        
        this.requestListener = SpringHelper.getRequestListener();
        LogService.appLog.debug("Application Request Listener configured is available: " + (this.requestListener != null));
        this.dccServiceEndpoint.setRequestListener(requestListener);
        
        try {
        	LogService.appLog.debug("DccService-start:Initiated...");
            this.dccServiceEndpoint.start();
            this.state = State.RUNNING;
            LogService.alarm(AlarmCode.SYSTEM_START_UP, (Object) null);
        } catch (ChargingStackLifeCycleException e) {
            LogService.appLog.error("DccService-start:Encountered Excception.Putting the stack to SHUTDOWN mode..",e);
            this.state = State.SHUTDOWN;
        }
    }

    @Override
    public void stop() {
        this.dccServiceEndpoint.stop();
        this.state = State.SHUTDOWN;
        LogService.alarm(AlarmCode.SYSTEM_SHUTDOWN, (Object)null);
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
    public void stop(Runnable arg0) {
        this.dccServiceEndpoint.stop();
        this.state = State.SHUTDOWN;
    }


    public State getState() {
        return state;
    }
    
    
}
