package com.satnar.air.ucip.client.internal;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.satnar.air.ucip.client.xmlrpc.XmlRpcClient;
import com.satnar.common.LogService;

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
	    LogService.appLog.debug("Simple Routes available: " + this.routes.size());
	    if (this.routes.size() == 0) {
	        LogService.appLog.error("No Routes available for selection. Check Stack Configuraiton or Initialize first!!");
	        return null;
	    }
		int nextRoute = currentRoute.incrementAndGet();
		if (nextRoute >= this.routes.size()) {
			nextRoute = 0;
			currentRoute.set(-1);
		}
		LogService.appLog.debug("Route Selected with index#" + nextRoute);
		return this.routes.get(nextRoute);
	}
	
	
	public XmlRpcClient getClientBySiteId(String siteId) {
	    LogService.appLog.debug("Site Routes available: " + this.siteRoutes.size());
        List<XmlRpcClient> sitePeers = this.siteRoutes.get(siteId);
		if (sitePeers == null) {
		    LogService.appLog.info("No Site Sepcific Routes available. Attempting simple route...");
			return this.getClient();
		}
		
		int nextRoute = this.sitePointer.get(siteId).incrementAndGet();
		if (nextRoute >= sitePeers.size()) {
			nextRoute = 0;
			this.sitePointer.get(siteId).set(-1);
		}
        LogService.appLog.debug("Route Selected with index#" + nextRoute);
		return sitePeers.get(nextRoute);
	}



    @Override
    public String toString() {
        return String.format("LoadBalancer [currentRouteIndex=%s, routes=%s, \n\nsiteRoutes=%s, \n\nsitePointer=%s]", currentRoute.get(), routes, siteRoutes, sitePointer);
    }
	
	
	
}
