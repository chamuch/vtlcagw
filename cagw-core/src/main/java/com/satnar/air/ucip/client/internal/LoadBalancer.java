package com.satnar.air.ucip.client.internal;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.satnar.air.ucip.client.xmlrpc.XmlRpcClient;

public class LoadBalancer {
	
	private static AtomicInteger currentRoute = new AtomicInteger(-1);
	
	private List<XmlRpcClient> routes = new ArrayList<XmlRpcClient>();
	private Map<String, List<XmlRpcClient>> siteRoutes = new Hashtable<>();
	private Map<String, AtomicInteger> sitePointer = new Hashtable<String, AtomicInteger>();
	
	public void addRoute(String site, XmlRpcClient airNode) {
	    this.routes.add(airNode);
	    if (this.siteRoutes.get(site) == null) {
	        this.siteRoutes.put(site, new ArrayList<XmlRpcClient>());
	    }
	    this.siteRoutes.get(site).add(airNode);
        this.sitePointer.put(site, new AtomicInteger(-1));
	}
	
	
	
	public XmlRpcClient getClient() {
		int nextRoute = currentRoute.incrementAndGet();
		if (nextRoute >= this.routes.size()) {
			nextRoute = 0;
			currentRoute.set(-1);
		}
		return this.routes.get(nextRoute);
	}
	
	
	public XmlRpcClient getClientBySiteId(String siteId) {
		List<XmlRpcClient> sitePeers = this.siteRoutes.get(siteId);
		if (sitePeers == null)
			return this.getClient();
		
		int nextRoute = this.sitePointer.get(siteId).incrementAndGet();
		if (nextRoute >= sitePeers.size()) {
			nextRoute = 0;
			this.sitePointer.get(siteId).set(-1);
		}
		return sitePeers.get(nextRoute);
	}
	
}
