package com.satnar.common.charging.diameter;

public interface LoadBalancerPool {
	
	Peer getPeerById(String hostId);
	
	Peer getPeerBySite(String site);
	
}
