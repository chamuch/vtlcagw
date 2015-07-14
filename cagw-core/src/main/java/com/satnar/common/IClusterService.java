package com.satnar.common;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

public interface IClusterService {

	public void init();
		
	public HazelcastInstance getInstance();
	
	public IAtomicLong getClusteredCounter(String name);
}
