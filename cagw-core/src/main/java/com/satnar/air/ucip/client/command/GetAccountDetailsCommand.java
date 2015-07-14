package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.GetAccountDetailsRequest;
import com.satnar.air.ucip.client.response.GetAccountDetailsResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class GetAccountDetailsCommand extends AbstractAirCommand<GetAccountDetailsResponse> {

	private GetAccountDetailsRequest request;
	
	public GetAccountDetailsCommand(GetAccountDetailsRequest request) {
		this.request = request;
	}

	@Override
	public GetAccountDetailsResponse execute() throws UcipException {
		GetAccountDetailsResponse response = new GetAccountDetailsResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
}
