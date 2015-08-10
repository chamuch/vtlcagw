package com.satnar.common.charging.diameter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.satnar.common.LogService;

public class RoundRobinLoadBalancer implements LoadBalancer {

	private String name;
	private AtomicInteger counter = new AtomicInteger(0);
	private final List<Peer> pool = new CopyOnWriteArrayList<Peer>();
	
	public RoundRobinLoadBalancer(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public void addPeer(Peer member) {
		pool.add(member);
	}

	@Override
	public void removePeer(Peer member) {
		pool.remove(member);
	}

	@Override
	public List<Peer> getPeers() {
		return pool;
	}

	@Override
	public Peer getRoute() {
		int size = pool.size();
		LogService.appLog.debug("Pool Size: {}", size);
		if (size <= 0) {
			throw new IllegalStateException("[RoundRobinLoadBalancer] Pool is empty!.");
		}

		int target = counter.getAndIncrement();
		while (target >= size) {
			counter.set(1); // reset counter
			target = 0;
		}

		LogService.appLog.debug("Route selected: " + pool.get(target));
		return pool.get(target);
	}

	@Override
	public Peer chooseRoute() {
		int size = pool.size();
		LogService.appLog.debug("Pool Size: {}", size);
        if (size <= 0) {
			throw new IllegalStateException("[RoundRobinLoadBalancer] Pool is empty!.");
		}

		int target = counter.getAndIncrement();
		while (target >= size) {
			counter.set(1); // reset counter
			target = 0;
		}

        LogService.appLog.debug("Route selected: " + pool.get(target));
		return pool.get(target);
	}

}
