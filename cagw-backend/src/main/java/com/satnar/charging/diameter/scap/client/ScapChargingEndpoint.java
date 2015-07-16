package com.satnar.charging.diameter.scap.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ericsson.pps.diameter.dccapi.DCCStack;
import com.ericsson.pps.diameter.rfcapi.base.DiameterConfig;
import com.ericsson.pps.diameter.rfcapi.base.DiameterConfigException;
import com.ericsson.pps.diameter.scapv2.SCAPStack;
import com.satnar.common.LogService;
import com.satnar.common.charging.ChargingStackLifeCycleException;
import com.satnar.common.charging.diameter.LoadBalancer;
import com.satnar.common.charging.diameter.Peer;

public class ScapChargingEndpoint implements IScapCharging {
    
    // Stack Literals
    private static final String SCAP_V2             = "SCAPV2";
    private static final String SERVICE_CONTEXT_ID  = "SCAP_V.2.0@ericsson.com";
    private static final String SERVICE_TCP_ADDRESS = "OwnIPAddress";
  
    // functional members
    private DCCStack            dccStack            = null;
    private Properties          config              = null;
    private List<Peer>          peers               = new ArrayList<Peer>();
    private LoadBalancer scapLoadBalancer = null;
    
    
    // config members
    private String              ownProductId        = null;
    private String              originRealm         = null;
    private String              ownFqdn             = null;
    private int                 ownTcpPort          = 0;
    private String              ownTcpAddress       = null;
    private int                 peerCount           = 0;
    private int                 threadPoolSize      = 0;
    private int                 eventQueueSize      = 0;
    private int                 sendQueueSize       = 0;
    private int                 sendMessageLimit    = 0;
    private long                supportedVendor     = 0;
    
    
    
    public ScapChargingEndpoint(Properties scapConfig) {
        this.config = scapConfig;
    }
    
    @Override
    public void start() throws ChargingStackLifeCycleException {
       
        ConfigHelper.validateAndInitializeConfig(this, this.config);
        this.prepareStack();
        
        try {
        	LogService.appLog.debug("ScapChargingEndpoint:Start:Initiated..");
            this.dccStack.start();
            LogService.appLog.debug("ScapChargingEndpoint:Start:Completed..");
        } catch (IOException e) {
            // TODO log for troubleshooting
        	LogService.stackTraceLog.debug("ScapChargingEndpoint-start:Unable to start the SCAP Stack:TcpAddress"+this.ownTcpAddress,e);
            throw new ChargingStackLifeCycleException("Unable to start the SCAP Stack with given config!!", e);
        } catch (DiameterConfigException e) {
            // TODO log for troubleshooting
        	LogService.stackTraceLog.debug("ScapChargingEndpoint-start:Unable to start the SCAP Stack:TcpAddress"+this.ownTcpAddress,e);
            throw new ChargingStackLifeCycleException("Unable to start the SCAP Stack with given config!!", e);
        }
    }
    
    @Override
    public void stop() {
        this.dccStack.stop();
    }
    
    
    
    private void prepareStack() throws ChargingStackLifeCycleException {
        this.dccStack = new SCAPStack();
        this.dccStack.setOwnProductId(this.ownProductId);
        this.dccStack.setOriginRealm(this.originRealm);
        this.dccStack.setOwnFqdn(this.ownFqdn);
        this.dccStack.setOwnTcpPort(this.ownTcpPort);
        this.dccStack.getDiameterConfig().setValue(SERVICE_TCP_ADDRESS, this.ownTcpAddress);
        this.loadStaticRoutes(this.dccStack, peers);
        this.dccStack.getDiameterConfig().setValue(DiameterConfig.NUMBER_OF_THREADS_THAT_HANDLES_RECEIVED_REQUESTS, this.threadPoolSize);
        this.dccStack.getDiameterConfig().setValue(DiameterConfig.EVENT_QUEUE_SIZE, this.eventQueueSize);
        this.dccStack.getDiameterConfig().setValue(DiameterConfig.SEND_QUEUE_SIZE, this.sendQueueSize);
        this.dccStack.getDiameterConfig().setValue(DiameterConfig.SEND_MESSAGE_LIMIT, this.sendMessageLimit);
        this.dccStack.getDiameterConfig().addSupportedVendor(this.supportedVendor);
        this.dccStack.getDiameterConfig().addSupportedVendor(10415);
        
        LogService.appLog.debug("ScapChargingEndpoint-prepareStack:Sucess - Realm: " + this.originRealm + ", Address: " + this.ownTcpAddress);
        
    }
    
    private void loadStaticRoutes(DCCStack dccStack, List<Peer> peers) throws ChargingStackLifeCycleException {
        
    	for (Peer peer: this.peers) {
            try {
                LogService.appLog.debug("Adding static route: " + peer);
                dccStack.addStaticRoute(peer.getRealm(), -1, -1, peer.getAddress());
            } catch (UnknownServiceException e) {
            	LogService.appLog.error("SCAP Stack refused configuring peer. Offenders - Realm: " + peer.getRealm() + ", Address: " + peer.getAddress(), e);
                throw new ChargingStackLifeCycleException("SCAP Stack refused configuring peer. Offenders - Realm: " + peer.getRealm() + ", Address: " + peer.getAddress());
            } catch (URISyntaxException e) {
            	LogService.appLog.error("SCAP Stack refused configuring peer. Offenders - Realm: " + peer.getRealm() + ", Address: " + peer.getAddress(), e);
                throw new ChargingStackLifeCycleException("SCAP Stack refused configuring peer. Offenders - Realm: " + peer.getRealm() + ", Address: " + peer.getAddress());
            }
        }
        LogService.appLog.debug("Configured DCC SCAP Stack with all peers");
          	
    	this.scapLoadBalancer = new LoadBalancer(peers);
        
    }
    
    
    // getters and setters

    public DCCStack getDccStack() {
        return dccStack;
    }

    public void setDccStack(DCCStack dccStack) {
        this.dccStack = dccStack;
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

    public LoadBalancer getScapLoadBalancer() {
		return scapLoadBalancer;
	}

	public void setScapLoadBalancer(LoadBalancer scapLoadBalancer) {
		this.scapLoadBalancer = scapLoadBalancer;
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

    public int getPeerCount() {
        return peerCount;
    }

    public void setPeerCount(int peerCount) {
        this.peerCount = peerCount;
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

    public long getSupportedVendor() {
        return supportedVendor;
    }

    public void setSupportedVendor(long supportedVendor) {
        this.supportedVendor = supportedVendor;
    }
    
    
     
}
