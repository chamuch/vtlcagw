package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.UpdateServiceClassRequest;
import com.satnar.air.ucip.client.response.UpdateServiceClassResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class UpdateServiceClassCommand extends AbstractAirCommand<UpdateServiceClassResponse> {

	private UpdateServiceClassRequest request;
	
	public UpdateServiceClassCommand(UpdateServiceClassRequest request) {
		this.request = request;
	}

	@Override
	public UpdateServiceClassResponse execute() throws UcipException {
		UpdateServiceClassResponse response = new UpdateServiceClassResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
			throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}

}
