package com.satnar.air.ucip.client.xmlrpc;

public abstract class XmlRpcResponse {
	
	protected Object result;
	
	public void setResult(Object result) {
		this.result =  result;
	}
}
