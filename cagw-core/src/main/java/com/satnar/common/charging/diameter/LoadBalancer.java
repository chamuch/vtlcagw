package com.satnar.common.charging.diameter;

import java.util.List;

public interface LoadBalancer {
	
	public abstract void addPeer(Peer peer);
	
	public abstract void removePeer(Peer peer);
	
	public abstract List<Peer> getPeers();
	
	public abstract Peer getRoute();
	
	public abstract Peer chooseRoute();
}
