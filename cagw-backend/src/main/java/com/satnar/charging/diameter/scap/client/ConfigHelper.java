package com.satnar.charging.diameter.scap.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import com.satnar.common.LogService;
import com.satnar.common.charging.ChargingStackLifeCycleException;
import com.satnar.common.charging.diameter.Peer;



public abstract class ConfigHelper {
    
    // config literals
    private static final String OWN_PRODUCT_ID = "ownProductId";
    private static final String ORIGIN_REALM = "originRealm";
    private static final String OWN_FQDN = "ownFqdn";
    private static final String OWN_TCP_PORT = "ownTcpPort";
    private static final String OWN_TCP_ADDRESS = "ownTcpAddress";
    private static final String PEER_COUNT = "peerCount";
    private static final String PEER_PREFIX = "peer.";
    private static final String PEER_ADDRESS = ".address";
    private static final String PEER_HOST_ID = ".hostId";
    private static final String PEER_REALM = ".realm";
    private static final String PEER_SITE_ID = ".siteId";
    private static final String THREAD_POOL_SIZE = "threadPoolSize";
    private static final String EVENT_QUEUE_SIZE = "eventQueueSize";
    private static final String SEND_QUEUE_SIZE = "sendQueueSize";
    private static final String SEND_MESSAGE_LIMIT = "sendMessageLimit";
    private static final String SUPPORTED_VENDOR = "supportedVendor";
    
    public ConfigHelper() {
        // TODO Auto-generated constructor stub
    }
    
    
    //TODO: move the config validation and init method here
    public static void validateAndInitializeConfig(ScapChargingEndpoint endpoint, Properties config) throws ChargingStackLifeCycleException {
        String param = null;
        
        // own product id
        param = endpoint.getConfig().getProperty(OWN_PRODUCT_ID);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'ownProductId' is not set!");
        endpoint.setOwnProductId(param);

        // origin realm
        param = endpoint.getConfig().getProperty(ORIGIN_REALM);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'originRealm' is not set!");
        endpoint.setOriginRealm(param);

        // origin realm
        param = endpoint.getConfig().getProperty(OWN_FQDN);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'ownFqdn' is not set!");
        endpoint.setOwnFqdn(param);
        
        // own tcp port
        param = endpoint.getConfig().getProperty(OWN_TCP_PORT);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'ownTcpPort' is not set!");
        try {
            endpoint.setOwnTcpPort(Integer.parseInt(param));
            if (endpoint.getOwnTcpPort() <= 0)
                throw new ChargingStackLifeCycleException("'ownTcpPort' is not valid! Found: " + param);
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'ownTcpPort' is not set!");
        }
        
        // own tcp address
        param = endpoint.getConfig().getProperty(OWN_TCP_ADDRESS);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'ownTcpAddress' is not set!");
        try {
            InetAddress.getByName(param);
            endpoint.setOwnTcpAddress(param);
        } catch (UnknownHostException e) {
            endpoint.setOwnTcpAddress("0.0.0.0");
        }
        
        // peer count
        param = endpoint.getConfig().getProperty(PEER_COUNT);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'peerCount' is not set!");
        
        try {
            endpoint.setPeerCount(Integer.parseInt(param));
            if (endpoint.getPeerCount() == 0) 
                throw new ChargingStackLifeCycleException("'peerCount' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'peerCount' is not set!");
        }
        
        // get peers
        for (int i = 1; i <= endpoint.getPeerCount(); i++) {
            Peer peer = new Peer();
            
            String key = PEER_PREFIX + i + PEER_ADDRESS;
            param = endpoint.getConfig().getProperty(key);
            if (param == null || param.equalsIgnoreCase(""))
                throw new ChargingStackLifeCycleException(key + " is not set or empty!");
            LogService.appLog.debug(key + "-" + param);
            peer.setAddress(param);
            
            key = PEER_PREFIX + i + PEER_HOST_ID;
            param = endpoint.getConfig().getProperty(key);
            if (param == null || param.equalsIgnoreCase(""))
                throw new ChargingStackLifeCycleException(key + " is not set or empty!");
            LogService.appLog.debug(key + "-" + param);
            peer.setHostId(param);
            
            key = PEER_PREFIX + i + PEER_REALM;
            param = endpoint.getConfig().getProperty(key);
            if (param == null || param.equalsIgnoreCase(""))
                throw new ChargingStackLifeCycleException(key + " is not set or empty!");
            LogService.appLog.debug(key + "-" + param);
            peer.setRealm(param);

            key = PEER_PREFIX + i + PEER_SITE_ID;
            param = endpoint.getConfig().getProperty(key);
            if (param == null || param.equalsIgnoreCase(""))
                throw new ChargingStackLifeCycleException(key + " is not set or empty!");
            LogService.appLog.debug(key + "-" + param);
            peer.setSiteId(param);

            endpoint.getPeers().add(peer);
        }
        
        // thread pool size
        param = endpoint.getConfig().getProperty(THREAD_POOL_SIZE);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'threadPoolSize' is not set!");
        
        try {
            endpoint.setThreadPoolSize(Integer.parseInt(param));
            if (endpoint.getThreadPoolSize() == 0) 
                throw new ChargingStackLifeCycleException("'threadPoolSize' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'threadPoolSize' is not set!");
        }
        
        // event queue size
        param = endpoint.getConfig().getProperty(EVENT_QUEUE_SIZE);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'eventQueueSize' is not set!");
        
        try {
            endpoint.setEventQueueSize(Integer.parseInt(param));
            if (endpoint.getEventQueueSize() == 0) 
                throw new ChargingStackLifeCycleException("'eventQueueSize' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'eventQueueSize' is not set!");
        }
        
        // send queue size
        param = endpoint.getConfig().getProperty(SEND_QUEUE_SIZE);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'sendQueueSize' is not set!");
        
        try {
            endpoint.setSendQueueSize(Integer.parseInt(param));
            if (endpoint.getSendQueueSize() == 0) 
                throw new ChargingStackLifeCycleException("'sendQueueSize' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'sendQueueSize' is not set!");
        }
        
        // send message limit
        param = endpoint.getConfig().getProperty(SEND_MESSAGE_LIMIT);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'sendMessageLimit' is not set!");
        
        try {
            endpoint.setSendMessageLimit(Integer.parseInt(param));
            if (endpoint.getSendMessageLimit() == 0) 
                throw new ChargingStackLifeCycleException("'sendMessageLimit' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'sendMessageLimit' is not set!");
        }
        
        // supported vendor
        param = endpoint.getConfig().getProperty(SUPPORTED_VENDOR);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'supportedVendor' is not set!");
        
        try {
            endpoint.setSupportedVendor(Long.parseLong(param));
            if (endpoint.getSupportedVendor() == 0) 
                throw new ChargingStackLifeCycleException("'supportedVendor' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'supportedVendor' is not set!");
        }


    }
}
