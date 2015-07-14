package com.satnar.charging.diameter.scap.client;

import com.satnar.charging.ChargingStackLifeCycleException;

public interface IScapCharging {
    
    public abstract void start() throws ChargingStackLifeCycleException;
    
    public abstract void stop();
    
    
}
