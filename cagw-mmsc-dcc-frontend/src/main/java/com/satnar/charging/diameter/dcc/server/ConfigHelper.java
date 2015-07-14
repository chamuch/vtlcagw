package com.satnar.charging.diameter.dcc.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import com.satnar.charging.ChargingStackLifeCycleException;

public abstract class ConfigHelper {
    
    // config literals
    private static final String OWN_PRODUCT_ID     = "ownProductId";
    private static final String ORIGIN_REALM       = "originRealm";
    private static final String OWN_FQDN           = "ownFqdn";
    private static final String OWN_TCP_PORT       = "ownTcpPort";
    private static final String OWN_TCP_ADDRESS    = "ownTcpAddress";
    private static final String THREAD_POOL_SIZE   = "threadPoolSize";
    private static final String EVENT_QUEUE_SIZE   = "eventQueueSize";
    private static final String SEND_QUEUE_SIZE    = "sendQueueSize";
    private static final String SEND_MESSAGE_LIMIT = "sendMessageLimit";
    private static final String ACCOUNT_ID         = "accountId";
    private static final String AUTH_ID            = "authId";
    private static final String VENDOR_ID          = "vendorId";
    private static final String MESSAGE_TIMEOUT    = "messageTimeout";    
    
    public static void validateAndInitializeConfig(DccServiceEndpoint endpoint, Properties config) throws ChargingStackLifeCycleException {
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
        
        // accountId
        param = endpoint.getConfig().getProperty(ACCOUNT_ID);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'accountId' is not set!");
        
        try {
            endpoint.setAccountId(Long.parseLong(param));
            if (endpoint.getAccountId() == 0) 
                throw new ChargingStackLifeCycleException("'accountId' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'accountId' is not set!");
        }
        
        // authId
        param = endpoint.getConfig().getProperty(AUTH_ID);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'authId' is not set!");
        
        try {
            endpoint.setAuthId(Long.parseLong(param));
            if (endpoint.getAuthId() == 0) 
                throw new ChargingStackLifeCycleException("'authId' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'authId' is not set!");
        }
        
        // vendorId
        param = endpoint.getConfig().getProperty(VENDOR_ID);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'vendorId' is not set!");
        
        try {
            endpoint.setVendorId(Long.parseLong(param));
//            if (endpoint.getVendorId() == 0) 
//                throw new ChargingStackLifeCycleException("'vendorId' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'vendorId' is not set!");
        }
        
        // message timeout
        param = endpoint.getConfig().getProperty(MESSAGE_TIMEOUT);
        if (param == null || param.equalsIgnoreCase(""))
            throw new ChargingStackLifeCycleException("'messageTimeout' is not set!");
        
        try {
            endpoint.setMessageTimeout(Long.parseLong(param));
            if (endpoint.getMessageTimeout() == 0) 
                throw new ChargingStackLifeCycleException("'messageTimeout' is set to Zero (0)");
        } catch (NumberFormatException e) {
            throw new ChargingStackLifeCycleException("'messageTimeout' is not set!");
        }
        
    }
    
}
