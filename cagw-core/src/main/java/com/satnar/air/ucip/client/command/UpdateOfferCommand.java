package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.UpdateOfferRequest;
import com.satnar.air.ucip.client.response.UpdateOfferResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class UpdateOfferCommand extends AbstractAirCommand<UpdateOfferResponse> {

	private UpdateOfferRequest request;
	
	public UpdateOfferCommand(UpdateOfferRequest request) {
		this.request = request;
	}

	@Override
	public UpdateOfferResponse execute() throws UcipException {
		UpdateOfferResponse response = new UpdateOfferResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
			throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
}
