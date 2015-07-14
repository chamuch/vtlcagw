package com.satnar.air.ucip.client.internal;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.satnar.air.ucip.client.AirClient;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcClientFactory;


public class CsAirContext implements ApplicationContextAware {

	private static ApplicationContext context = null;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		CsAirContext.context = applicationContext;
	}
	
	public static AirClient getAirClient() {
		return context.getBean(AirClient.class);
	}
	
	public static XmlRpcClientFactory getXmlRpcClientFactory() {
		return context.getBean(XmlRpcClientFactory.class);
	}
	

}
