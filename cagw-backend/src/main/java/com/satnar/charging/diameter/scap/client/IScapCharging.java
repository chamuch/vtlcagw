package com.satnar.charging.diameter.scap.client;

import com.satnar.common.charging.ChargingStackLifeCycleException;



public interface IScapCharging {
    
    public abstract void start() throws ChargingStackLifeCycleException;
    
    public abstract void stop();
    
    
}
