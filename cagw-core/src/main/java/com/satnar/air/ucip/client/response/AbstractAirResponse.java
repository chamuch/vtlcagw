package com.satnar.air.ucip.client.response;

import java.io.Serializable;
import java.util.Map;

import com.satnar.air.ucip.client.xmlrpc.XmlRpcResponse;


public abstract class AbstractAirResponse extends XmlRpcResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer responseCode;

	public int getResponseCode() {
		if (responseCode != null) {
			return responseCode;
		}
		
		if(result == null)
		    return 1111;
		
		responseCode = (Integer) (((Map<?, ?>) result).get("responseCode"));
		return responseCode;
	}

	public boolean isResponseAvailable() {
		return result != null;
	}
	
	public Object getResult() {
		return result;
	}
	
}
