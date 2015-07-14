package com.ericsson.raso.cac.cagw.dao;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.satnar.common.LogService;

public class DataStaxConnection {

	private static DataStaxConnection instance;
	protected static Cluster cluster;
	protected static Session session;
	
	private String address = null;
	private String keyspace = null;

	public static synchronized DataStaxConnection getInstance(String cassandraAddress, String keySpaceName) {
		if (instance == null) {
			instance = new DataStaxConnection(cassandraAddress, keySpaceName);
		}
		return instance;
	}
	
	public static DataStaxConnection getInstance() {
	    if (instance == null)
	        throw new IllegalStateException("The Cassandra connection is not initialized. Cannot Proceed!!");
	    return instance;
	}

	/**
	 * Creating Cassandra connection using Datastax API
	 * @param cassandraAddress 
	 * @param keySpaceName 
	 *
	 */
	private DataStaxConnection(String cassandraAddress, String keySpaceName) {
		try {
		    this.address = cassandraAddress;
		    this.keyspace = keySpaceName;
		    
		    Cluster.Builder builder = Cluster.builder();
		    
		    String[] clusterAddress = cassandraAddress.split(",");
		    for (String nodeAddress: clusterAddress) {
		        builder = builder.addContactPoint(nodeAddress);
		    }
		    
			this.cluster = builder.build();			
			LogService.appLog.debug("DataStaxConnection: Cluster build is successful");
			this.session = cluster.connect(keySpaceName);
			LogService.appLog.debug("DataStaxConnection: Session established successfully");
		} catch (Exception e) {
			LogService.appLog.debug("DataStaxConnection: Encoutnered exception:",e);
			throw new RuntimeException(e);
		}
	}

	public static Cluster getCluster() {
		return cluster;
	}

	public static Session getSession() {
		return session;
	}

    public String getAddress() {
        return address;
    }

    public String getKeyspace() {
        return keyspace;
    }

}
