package com.satnar.common;

import java.util.concurrent.atomic.AtomicInteger;

import com.hazelcast.core.IAtomicLong;

public class WaterGate implements IngressValve {
    
    private String valveLabel = null;
    private int localWatermark = 0;
    private AtomicInteger localCounter = new AtomicInteger(0);
    private int systemWatermark = 0;
    //private ClusterService clusterService = null;
    private IClusterService clusterService = null;
    
    public WaterGate(String frontend, int nodeThreshold, int systemWideThreshold) {
        this.valveLabel = frontend;
        this.localWatermark = nodeThreshold;
        this.systemWatermark = systemWideThreshold;
        this.clusterService = SpringHelper.getClusterService();
    }
    
    @Override
    public boolean authorizeIngress() {
        if ((localCounter.get() +1) == this.localWatermark) {
        	LogService.appLog.error("WaterGate-authorizeIngress:Incoming Traffic exceeded node threshold !!! localCount:"+localCounter+"Node Threshold:"+this.localWatermark);
            return false;
        } else {
            IAtomicLong clusteredCounter = this.clusterService.getClusteredCounter(this.valveLabel);
            if ((clusteredCounter.get() + 1) == this.systemWatermark) {
            	LogService.appLog.error("WaterGate-authorizeIngress:Incoming Traffic exceeded system threshold !!! ClusterCount:"+clusteredCounter+"System Threshold:"+this.systemWatermark);
                return false;
            } else {
                this.localCounter.incrementAndGet();
                clusteredCounter.incrementAndGet();
                return true;
            }
        }
    }
    
    @Override
    public void updateExgress() {
        if ((this.localCounter.get() - 1) < 0) {
            this.localCounter.set(0);
        } else {
            this.localCounter.decrementAndGet();
        }
        
        IAtomicLong clusteredCounter = this.clusterService.getClusteredCounter(this.valveLabel);
        if ((clusteredCounter.get() - 1) < 0) {
            clusteredCounter.set(0);
        } else {
            clusteredCounter.decrementAndGet();
        }
        
    }
    
}
