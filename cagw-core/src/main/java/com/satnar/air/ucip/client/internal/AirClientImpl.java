package com.satnar.air.ucip.client.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.satnar.air.ucip.client.AirClient;
import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.request.AbstractAirRequest;
import com.satnar.air.ucip.client.response.AbstractAirResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcClient;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;
import com.satnar.common.LogService;


public class AirClientImpl implements AirClient {
    
    private Properties config = null;
	
	private String originNodeType;
	private String originHostName;
	private String defaultSite;
	private String defaultNai;
	private int negotiatedCapabilities = Integer.MAX_VALUE;

	private LoadBalancer loadBalancer = new LoadBalancer();
	
	
	public AirClientImpl(Properties ucipConfig)  {		
		LogService.appLog.info("AirClientImpl:AIR Endpoint initialization starts!!!");
		this.config = ucipConfig;
	}

	public void init() throws UcipException {
		ConfigHelper.initializeConfig(this);
		LogService.appLog.debug("Verifying init config: " + this.toString());
	}

	@Override
	public <R extends AbstractAirRequest, S extends AbstractAirResponse> void execute(R request, S response)
			throws XmlRpcException {
		if(request.getOriginTransactionId() == null) {
			request.setOriginTransactionId(String.valueOf(createTransactionId()));
		}
		
		LogService.appLog.debug("Verifying stack config: " + this.toString());
		if (request.getSubscriberNumberNAI() == null)
		    request.setSubscriberNumberNAI(Integer.valueOf(this.defaultNai));
		request.setOriginTimeStamp(new Date());
		request.setOriginHostName(originHostName);
		request.setOriginNodeType(originNodeType);

		
		try {
			LogService.appLog.info(request.getClass().getName()+"."+request.getMethodName() +"() - Fetching client for siteId:"+request.getSiteId());
			XmlRpcClient xmlRpcClient = getXmlRpcClient(request.getSubscriberNumber(), request.getSiteId());
			LogService.appLog.debug("Selected Routed to UCIP EP available: " + (xmlRpcClient != null));
			if (xmlRpcClient != null) {
			    LogService.stackTraceLog.info("UCIP.REQ >> " + request.toString());
			    xmlRpcClient.execute(request, response);
			    LogService.stackTraceLog.info("UCIP.RES >> " + response.toString());
			} else {
			    throw new XmlRpcException(999, "UCIP Stack had no available routes to send request!!");
			}
			
			LogService.appLog.info(request.getClass().getName()+"."+request.getMethodName() +"() Success:Msisdn:"+request.getSubscriberNumber()+":ResponseCode:"+response.getResponseCode());
		} catch (XmlRpcException e) {
			LogService.appLog.debug(request.getClass().getName()+"."+request.getMethodName() +"() Success:Msisdn:"+request.getSubscriberNumber()+":ResponseCode:"+response.getResponseCode());
			if(e.code == 0) {
				throw new XmlRpcException(e.getMessage(), e.linkedException);
			} else {
				throw e;
			}
		} catch (UcipException e) {
			throw new XmlRpcException(e.getCode(), e.getMessage(), e);
		}
		
		if(response.isResponseAvailable()) {
			int responseCode = response.getResponseCode();
			if(responseCode != 0) {
				throw new XmlRpcException(responseCode, responseCode + " CS-AIR for msisdn: " + request.getSubscriberNumber());
			}
		}
	}
	
    public void addSitePeer(String site, XmlRpcClient rpcClient) {
        this.loadBalancer.addRoute(site, rpcClient);
    }
    

	
	private static long createTransactionId() {
		return 9999 + (int)(Math.random() * ((999999999 - 9999) + 1));
	}
	
	private XmlRpcClient getXmlRpcClient(String msisdn, String site) throws UcipException {
		if(site!=null) {
			return this.loadBalancer.getClientBySiteId(site);
		} 
		return this.loadBalancer.getClient();
	}

    public Properties getConfig() {
        return config;
    }

    public String getOriginNodeType() {
        return originNodeType;
    }

    public void setOriginNodeType(String originNodeType) {
        this.originNodeType = originNodeType;
    }

    public String getOriginHostName() {
        return originHostName;
    }

    public void setOriginHostName(String originHostName) {
        this.originHostName = originHostName;
    }

    public String getDefaultSite() {
        return defaultSite;
    }

    public void setDefaultSite(String defaultSite) {
        this.defaultSite = defaultSite;
    }

    public String getDefaultNai() {
        return defaultNai;
    }

    public void setDefaultNai(String defaultNai) {
        this.defaultNai = defaultNai;
    }

    public int getNegotiatedCapabilities() {
        return negotiatedCapabilities;
    }

    public void setNegotiatedCapabilities(int negotiatedCapabilities) {
        this.negotiatedCapabilities = negotiatedCapabilities;
    }

    @Override
    public String toString() {
        return String.format("AirClientImpl [originNodeType=%s, originHostName=%s, defaultSite=%s, defaultNai=%s, negotiatedCapabilities=%s]\n\nloadBalancer=%s \n\nconfig=%s]",
                originNodeType,
                originHostName,
                defaultSite,
                defaultNai,
                negotiatedCapabilities,
                loadBalancer,
                config);
    }


	
}
