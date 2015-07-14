package com.satnar.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

public class ClusterService implements IClusterService{
    
    private HazelcastInstance instance = null;
    private static final String HZ_CONFIG = "CONFIG_HOME";

    public ClusterService() {
        init();
    }
    
    public void init() {
        String configHome = System.getenv(HZ_CONFIG);
        try {
            Boolean compress = false;

            FileInputStream fis = new FileInputStream(configHome + getFileSeparator() + "hazelcast-client.xml");
            ClientConfig config = new XmlClientConfigBuilder(fis).build();

            instance = HazelcastClient.newHazelcastClient(config);
         
        } catch (FileNotFoundException e) {
            // TODO: Throw SNMP Unable load cluster configuration. Cannot start
        	LogService.appLog.debug("ClusterService-:initCan not start cluster!!",e);
            //e.printStackTrace();
        }
    }
    
    public IAtomicLong getClusteredCounter(String name) {
        return instance.getAtomicLong(name);
    }
    
    public HazelcastInstance getInstance() {
		return instance;
	}    
    
    private String getFileSeparator() {
        String your_os = System.getProperty("os.name").toLowerCase();
        if (your_os.indexOf("win") >= 0) {
            return "\\";
        } else if (your_os.indexOf("nix") >= 0 || your_os.indexOf("nux") >= 0) {
            return "/";
        } else {
            return "/";
        }
    }
    
}
