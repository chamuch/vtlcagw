package com.satnar.air.ucip.client.xmlrpc;

public interface XmlRpcClientFactory {

	XmlRpcClient create(ConfigParams params) throws XmlRpcException;

}
