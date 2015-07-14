package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.UpdateFaFListRequest;
import com.satnar.air.ucip.client.response.UpdateFaFListResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class UpdateFaFListCommand extends AbstractAirCommand<UpdateFaFListResponse> {

	private UpdateFaFListRequest request;
	
	public UpdateFaFListCommand(UpdateFaFListRequest request) {
		this.request = request;
	}

	@Override
	public UpdateFaFListResponse execute() throws UcipException {
		UpdateFaFListResponse response = new UpdateFaFListResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
			throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
}