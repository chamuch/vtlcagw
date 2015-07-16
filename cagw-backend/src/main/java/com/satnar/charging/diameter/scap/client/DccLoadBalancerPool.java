package com.satnar.charging.diameter.scap.client;

import java.util.List;

import com.ericsson.pps.diameter.rfcapi.base.PeerConnectionListener;
import com.satnar.common.LogService;
import com.satnar.common.charging.diameter.DiameterLoadBalancerPool;
import com.satnar.common.charging.diameter.Peer;


public class DccLoadBalancerPool extends DiameterLoadBalancerPool implements PeerConnectionListener {

	public DccLoadBalancerPool(List<Peer> routes) {
		super(routes);
	}
	
	@Override
	public void peerAdded(String uri, boolean flag) {
		LogService.appLog.info("Peer Added: " + uri + " " + flag);
		addToPool(uri);
	}

	@Override
	public void peerConnected(String uri) {
	    LogService.appLog.info("Peer connected :: " + uri);
		//addToPool(uri);
	}

	@Override
	public void peerDisconnected(String uri,  int reason, int dprCause) {
	    LogService.appLog.info("Peer disconnected :: " + uri + " || Reason : " + reason + " || DPR Cause " + dprCause);
		//removeFromPool(uri);
	}
	
	@Override
	public void peerRemoved(String uri) {
	    LogService.appLog.info("Peer Removed :: " + uri);
		removeFromPool(uri);
	}

}