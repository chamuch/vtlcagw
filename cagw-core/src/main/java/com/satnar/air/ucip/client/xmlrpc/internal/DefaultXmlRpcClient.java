package com.satnar.air.ucip.client.xmlrpc.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.satnar.air.ucip.client.xmlrpc.XmlRpcClient;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcRequest;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcResponse;
import com.satnar.common.LogService;


public class DefaultXmlRpcClient implements XmlRpcClient {
	
	//private static Logger logger = LoggerFactory.getLogger(DefaultXmlRpcClient.class);
	private final Logger logger = LogService.stackTraceLog;

	private org.apache.xmlrpc.client.XmlRpcClient nativeClient;

	public void setNativeClient(org.apache.xmlrpc.client.XmlRpcClient nativeClient) {
		this.nativeClient = nativeClient;
	}

	@Override
	public <R extends XmlRpcRequest, S extends XmlRpcResponse> void execute(R request, S response) throws XmlRpcException {
		try {
			Object result = nativeClient.execute(request.getMethodName(), request.getParams());
			response.setResult(result);
		} catch (org.apache.xmlrpc.XmlRpcException e) {
			LogService.appLog.error(e.getMessage(), e);
			if(e.linkedException != null) {
			    LogService.appLog.error(e.linkedException.getMessage(), e.linkedException);
			}
			throw new XmlRpcException(e.code, e.getMessage(), e.linkedException);
		}
	}

}
