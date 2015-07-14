package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.GetBalanceAndDateRequest;
import com.satnar.air.ucip.client.response.GetBalanceAndDateResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;



public class GetBalanceAndDateCommand extends AbstractAirCommand<GetBalanceAndDateResponse> {

	private GetBalanceAndDateRequest request;

	public GetBalanceAndDateCommand(GetBalanceAndDateRequest request) {
		this.request = request;
	}

	@Override
	public GetBalanceAndDateResponse execute() throws UcipException {
		GetBalanceAndDateResponse response = new GetBalanceAndDateResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
}
