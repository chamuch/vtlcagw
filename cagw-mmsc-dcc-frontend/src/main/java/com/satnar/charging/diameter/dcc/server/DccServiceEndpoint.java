package com.satnar.charging.diameter.dcc.server;

import java.io.IOException;
import java.util.Properties;

import com.ericsson.pps.diameter.dccapi.DCCStack;
import com.ericsson.pps.diameter.rfcapi.base.ApplicationAlreadyInUseException;
import com.ericsson.pps.diameter.rfcapi.base.DiameterConfig;
import com.ericsson.pps.diameter.rfcapi.base.DiameterConfigException;
import com.ericsson.pps.diameter.rfcapi.base.PeerConnectionListener;
import com.ericsson.pps.diameter.rfcapi.base.avp.avpdatatypes.ApplicationId;
import com.ericsson.pps.diameter.rfcapi.base.message.ApplicationRequestListener;
import com.satnar.common.LogService;
import com.satnar.common.charging.ChargingStackLifeCycleException;

public class DccServiceEndpoint implements DiameterServiceEndpoint {
    
    // Stack Literals
    private static final String        SERVICE_TCP_ADDRESS        = "OwnIPAddress";
    
    // functional members
    private DCCStack                   dccStack                   = null;
    private Properties                 config                     = null;
    private String                     endpointId                 = null;
    private ApplicationId              applicationId              = null;
    private PeerConnectionListener     peerConnectionListener     = null;
    private ApplicationRequestListener applicationRequestListener = null;
    
    // config members
    private String                     originRealm                = null;
    private boolean                    acceptUnknownPeers         = true;
    private long                       accountId                  = 0;
    private long                       authId                     = 0;
    
    private String                     ownProductId               = null;
    private String                     ownFqdn                    = null;
    private int                        ownTcpPort                 = 0;
    private String                     ownTcpAddress              = null;
    private int                        threadPoolSize             = 0;
    private int                        eventQueueSize             = 0;
    private int                        sendQueueSize              = 0;
    private int                        sendMessageLimit           = 0;
    private long                       vendorId                   = 0;
    private long                       messageTimeout             = 0;
    
        
    
    
    
    @Override
    public void start() throws ChargingStackLifeCycleException {
        ConfigHelper.validateAndInitializeConfig(this, this.config);
        this.prepareStack();
        
        try {
            this.dccStack.start();
        } catch (IOException e) {
            // TODO log for troubleshooting
        	LogService.stackTraceLog.debug("DccServiceEndpoint-start:Unable to start the DCC Stack: originRealm:"+this.originRealm+":TcpAddress:"+this.ownTcpAddress,e);
            throw new ChargingStackLifeCycleException("Unable to start the DCC Stack with given config!!", e);
        } catch (DiameterConfigException e) {
            // TODO log for troubleshooting
        	LogService.stackTraceLog.debug("DccServiceEndpoint-start:Unable to start the DCC Stack: originRealm:"+this.originRealm+":TcpAddress:"+this.ownTcpAddress,e);
            throw new ChargingStackLifeCycleException("Unable to start the DCC Stack with given config!!", e);
        }
    }
    
    @Override
    public void stop() {
        this.dccStack.stop();
    }

    
    @Override
    public void setRequestListener(ApplicationRequestListener requestListener) {
        this.applicationRequestListener = requestListener;        
    }

    @Override
    public void setPeerConnectionListener(PeerConnectionListener peerConnectionListener) {
        this.peerConnectionListener = peerConnectionListener;
    }
    


    private void prepareStack() throws ChargingStackLifeCycleException {
        try {
            this.dccStack = new DCCStack();        
            this.applicationId = new ApplicationId(this.vendorId, this.accountId, this.authId);
            
            // TODO: Set the Timeout directly in the front end project where the request listener is implemented
            
            this.dccStack.getDiameterConfig().addApplication(this.applicationId);
            this.dccStack.setOwnFqdn(this.ownFqdn);
            this.dccStack.getDiameterConfig().setValue(DiameterConfig.DEFAULT_MESSAGE_TIMEOUT, this.messageTimeout);
            this.dccStack.setOwnProductId(this.ownProductId);
            this.dccStack.setOriginRealm(this.originRealm);
            this.dccStack.setOwnDiameterUri("aaa://" + this.ownTcpAddress + ":" + this.ownTcpPort + ";transport=tcp");
            //this.dccStack.getDiameterStack().addPeerConnectionListener(this.peerConnectionListener);
            //this.dccStack.getDiameterConfig().addRequestListener(this.applicationRequestListener, this.applicationId);
            this.dccStack.getDiameterConfig().setValue(DiameterConfig.OWN_IP_ADDRESS, this.ownTcpAddress);
            this.dccStack.getDiameterConfig().setValue(DiameterConfig.ACCEPT_UNKNOWN_PEERS, this.acceptUnknownPeers);
            this.dccStack.getDiameterConfig().setValue(DiameterConfig.NUMBER_OF_THREADS_THAT_HANDLES_RECEIVED_REQUESTS, this.threadPoolSize);
            this.dccStack.getDiameterConfig().setValue(DiameterConfig.EVENT_QUEUE_SIZE, this.eventQueueSize);
            this.dccStack.getDiameterConfig().setValue(DiameterConfig.SEND_QUEUE_SIZE, this.sendQueueSize);
            this.dccStack.getDiameterConfig().setValue(DiameterConfig.SEND_MESSAGE_LIMIT, this.sendMessageLimit);
            
            //this.dccStack.getDiameterStack().addPeerConnectionListener(this.peerConnectionListener);
            this.dccStack.getDiameterConfig().addRequestListener(this.applicationRequestListener, this.applicationId);
                
            LogService.stackTraceLog.debug("DccServiceEndpoint-prepareStack:Success: originRealm:"+this.originRealm+":TcpAddress:"+this.ownTcpAddress);
        } catch (ApplicationAlreadyInUseException e) {
            // TODO log for troubleshooting
        	LogService.stackTraceLog.debug("DccServiceEndpoint-start:Unable to start the DCC Stack: originRealm:"+this.originRealm+":TcpAddress:"+this.ownTcpAddress,e);
            throw new ChargingStackLifeCycleException("Unable to start the DCC Stack with given config!!", e);
        }
        
        
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public String getOwnProductId() {
        return ownProductId;
    }

    public void setOwnProductId(String ownProductId) {
        this.ownProductId = ownProductId;
    }

    public String getOriginRealm() {
        return originRealm;
    }

    public void setOriginRealm(String originRealm) {
        this.originRealm = originRealm;
    }

    public String getOwnFqdn() {
        return ownFqdn;
    }

    public void setOwnFqdn(String ownFqdn) {
        this.ownFqdn = ownFqdn;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ApplicationId applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isAcceptUnknownPeers() {
        return acceptUnknownPeers;
    }

    public void setAcceptUnknownPeers(boolean acceptUnknownPeers) {
        this.acceptUnknownPeers = acceptUnknownPeers;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getAuthId() {
        return authId;
    }

    public void setAuthId(long authId) {
        this.authId = authId;
    }

    public int getOwnTcpPort() {
        return ownTcpPort;
    }

    public void setOwnTcpPort(int ownTcpPort) {
        this.ownTcpPort = ownTcpPort;
    }

    public String getOwnTcpAddress() {
        return ownTcpAddress;
    }

    public void setOwnTcpAddress(String ownTcpAddress) {
        this.ownTcpAddress = ownTcpAddress;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public int getEventQueueSize() {
        return eventQueueSize;
    }

    public void setEventQueueSize(int eventQueueSize) {
        this.eventQueueSize = eventQueueSize;
    }

    public int getSendQueueSize() {
        return sendQueueSize;
    }

    public void setSendQueueSize(int sendQueueSize) {
        this.sendQueueSize = sendQueueSize;
    }

    public int getSendMessageLimit() {
        return sendMessageLimit;
    }

    public void setSendMessageLimit(int sendMessageLimit) {
        this.sendMessageLimit = sendMessageLimit;
    }

    public long getVendorId() {
        return vendorId;
    }

    public void setVendorId(long vendorId) {
        this.vendorId = vendorId;
    }

    public long getMessageTimeout() {
        return messageTimeout;
    }

    public void setMessageTimeout(long messageTimeout) {
        this.messageTimeout = messageTimeout;
    }

    
    
}
