package com.satnar.air.ucip.client.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.InstallSubscriberRequest;
import com.satnar.air.ucip.client.response.InstallSubscriberResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class InstallSubscriberCommand extends AbstractAirCommand<InstallSubscriberResponse>{
	Logger logger = LoggerFactory.getLogger(InstallSubscriberCommand.class);
	private InstallSubscriberRequest request;
	
	public InstallSubscriberCommand(InstallSubscriberRequest installSubscriberRequest) {
		this.request = installSubscriberRequest;
	}

	@Override
	public InstallSubscriberResponse execute() throws UcipException {
		InstallSubscriberResponse response =  new InstallSubscriberResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
			logger.debug("XMLRpc execution failure", e);
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
}
