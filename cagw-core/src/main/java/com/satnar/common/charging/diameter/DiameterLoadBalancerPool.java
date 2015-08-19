package com.satnar.common.charging.diameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.satnar.common.LogService;

public class DiameterLoadBalancerPool implements LoadBalancerPool {

    private Map<String, Peer> hostRouteMap = new HashMap<String, Peer>();
	private Map<String, List<Peer>> siteMap = new HashMap<String, List<Peer>>();

	private Map<String, LoadBalancer> loadBalancerMap = new HashMap<String, LoadBalancer>();

	public DiameterLoadBalancerPool(List<Peer> routes) {
		init(routes);
	}

	private void init(List<Peer> routes) {
        LogService.appLog.debug("SCAP Peer LB init");
		for (Peer peer : routes) {
			hostRouteMap.put(peer.getHostId(), peer);
			LogService.appLog.debug("Adding Host Route to LB: " + peer);
            
			List<Peer> list = siteMap.get(peer.getSiteId());
			if (list == null) {
				list = new ArrayList<Peer>();
				siteMap.put(peer.getSiteId(), list);
                LogService.appLog.debug("Adding Site Peer to LB: " + peer);
			}
			list.add(peer);
			this.addToPool(peer.getHostId());
            LogService.appLog.debug("Adding Site Routes to LB: " + peer);
		}
	}

	protected void addToPool(String uri) {
		Peer route = getPeerByUri(uri);
		if (route != null) {
			LoadBalancer loadBalancer = loadBalancerMap.get(route.getSiteId());
			if (loadBalancer == null) {
				loadBalancer = new RoundRobinLoadBalancer(route.getSiteId());
				loadBalancerMap.put(route.getSiteId(), loadBalancer);
			}

			if (!loadBalancer.getPeers().contains(route)) {
				loadBalancer.getPeers().add(route);
			}
		}
	}

	protected void removeFromPool(String uri) {
		Peer route = getPeerByUri(uri);
		if (route != null) {
			LoadBalancer loadBalancer = loadBalancerMap.get(route.getSiteId());
			if (loadBalancer == null) {
				loadBalancer = new RoundRobinLoadBalancer(route.getSiteId());
				loadBalancerMap.put(route.getSiteId(), loadBalancer);
			}
			loadBalancer.getPeers().remove(route);
		}
	}

	private Peer getPeerByUri(String uri) {
	    LogService.appLog.debug("URI : " + uri);
		for (Peer route : hostRouteMap.values()) {
			if (uri.contains(route.getHostId())) {
				return route;
			}
		}
		return null;
	}

	@Override
	public Peer getPeerById(String hostId) {
		return hostRouteMap.get(hostId);
	}

	@Override
	public Peer getPeerBySite(String site) {
	    LogService.appLog.debug("loadBalancerMap for site: " + site + " is size: " + loadBalancerMap.size());
		LoadBalancer balancer = loadBalancerMap.get(site);
		if (balancer == null) {
		    LogService.appLog.error("Load Balancer for [{}] cannot be found.", site);
			if (loadBalancerMap.isEmpty()) {
			    LogService.appLog.error("No load balancer is registered.");
			} else {
			    LogService.appLog.error("Available load balancers for sites include: {}", loadBalancerMap.keySet());
			}
			throw new IllegalStateException("Load Balancer for [" + site + "] is not found.");
		}
		LogService.appLog.debug("Site based route will be selected now!");
		return balancer.chooseRoute();
	}
}
