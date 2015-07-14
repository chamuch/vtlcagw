package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.DeleteSubscriberRequest;
import com.satnar.air.ucip.client.response.DeleteSubscriberResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;



public class DeleteSubscriberCommand extends AbstractAirCommand<DeleteSubscriberResponse> {

	private DeleteSubscriberRequest request;
	
	public DeleteSubscriberCommand(DeleteSubscriberRequest request) {
		this.request = request;
	}

	@Override
	public DeleteSubscriberResponse execute() throws UcipException {
		DeleteSubscriberResponse response = new DeleteSubscriberResponse(endpointId);
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
}
