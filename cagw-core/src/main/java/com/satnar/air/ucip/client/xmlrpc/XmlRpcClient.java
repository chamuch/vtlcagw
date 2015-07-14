package com.satnar.air.ucip.client.xmlrpc;


public interface XmlRpcClient {
	
	<R extends XmlRpcRequest,S extends XmlRpcResponse> void execute(R request, S response) throws XmlRpcException;
}
