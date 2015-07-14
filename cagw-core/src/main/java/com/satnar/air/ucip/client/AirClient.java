package com.satnar.air.ucip.client;

import com.satnar.air.ucip.client.request.AbstractAirRequest;
import com.satnar.air.ucip.client.response.AbstractAirResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public interface AirClient {
	
	<R extends AbstractAirRequest,S extends AbstractAirResponse> void execute(R request, S response) throws XmlRpcException;
	
}
