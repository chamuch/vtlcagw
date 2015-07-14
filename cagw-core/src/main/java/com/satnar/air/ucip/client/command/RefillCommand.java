package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.RefillRequest;
import com.satnar.air.ucip.client.response.RefillResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;



public class RefillCommand extends AbstractAirCommand<RefillResponse> {

	private RefillRequest request;

	public RefillCommand(RefillRequest refillRequest) {
		this.request = refillRequest;
	}

	@Override
	public RefillResponse execute() throws UcipException {
		RefillResponse response = new RefillResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}

		return response;
	}

}
