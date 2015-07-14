package com.satnar.charging.diameter.dcc.server;

import com.ericsson.pps.diameter.rfcapi.base.PeerConnectionListener;
import com.ericsson.pps.diameter.rfcapi.base.message.ApplicationRequestListener;
import com.satnar.common.charging.ChargingStackLifeCycleException;


public interface DiameterServiceEndpoint {
    
    public abstract void start() throws ChargingStackLifeCycleException;
    
    public abstract void stop();
    
    public abstract void setRequestListener(ApplicationRequestListener requestListener);
    
    public abstract void setPeerConnectionListener(PeerConnectionListener peerConnectionListener);
    
    
    
}