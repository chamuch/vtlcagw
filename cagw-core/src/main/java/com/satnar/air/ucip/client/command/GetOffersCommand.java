package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.GetOffersRequest;
import com.satnar.air.ucip.client.response.GetOffersResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class GetOffersCommand extends AbstractAirCommand<GetOffersResponse> {

	private GetOffersRequest request;

	public GetOffersCommand(GetOffersRequest request) {
		this.request = request;
	}

	@Override
	public GetOffersResponse execute() throws UcipException {
		GetOffersResponse response = new GetOffersResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
}
