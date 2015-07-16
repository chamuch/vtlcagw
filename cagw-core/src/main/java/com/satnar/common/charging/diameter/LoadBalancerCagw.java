package com.satnar.common.charging.diameter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.satnar.common.LogService;

public class LoadBalancerCagw {
	
	private static AtomicInteger currentRoute = new AtomicInteger(-1);
	
	private List<Peer> routes = new ArrayList<Peer>();
	private Map<String, Peer> hostRoutes = new Hashtable<>();
	private Map<String, List<Peer>> siteRoutes = new Hashtable<>();
	private Map<String, AtomicInteger> sitePointer = new Hashtable<String, AtomicInteger>();
	
	public LoadBalancerCagw(List<Peer> routes) {
	    LogService.appLog.debug("SCAP Peer LB init");
		this.routes.addAll(routes);
		
		for (Peer peer: routes) {
			this.hostRoutes.put(peer.getHostId(), peer);
			LogService.appLog.debug("Adding Host Route to LB: " + peer);
			
			List<Peer> sitePeers = this.siteRoutes.get(peer.getSiteId());
			if (sitePeers == null)  {
				sitePeers = new ArrayList<Peer>();
				this.sitePointer.put(peer.getSiteId(), new AtomicInteger(-1));
				LogService.appLog.debug("Adding Site Peer to LB: " + peer);
			}
			sitePeers.add(peer);
			this.siteRoutes.put(peer.getSiteId(), sitePeers);
            LogService.appLog.debug("Adding Site Routes to LB: " + peer);
		}
	}
	
	public Peer getRoute() {
		int nextRoute = currentRoute.incrementAndGet();
		if (nextRoute >= this.routes.size()) {
			nextRoute = 0;
			currentRoute.set(-1);
		}
		return this.routes.get(nextRoute);
	}
	
	public Peer getRouteByHostId(String hostId) {
		return this.hostRoutes.get(hostId);
	}
	
	public Peer getRouteBySiteId(String siteId) {
		List<Peer> sitePeers = this.siteRoutes.get(siteId);
		if (sitePeers == null)
			return this.getRoute();
		
		int nextRoute = this.sitePointer.get(siteId).incrementAndGet();
		if (nextRoute >= sitePeers.size()) {
			nextRoute = 0;
			this.sitePointer.get(siteId).set(-1);
		}
		return sitePeers.get(nextRoute);
	}
	
}
