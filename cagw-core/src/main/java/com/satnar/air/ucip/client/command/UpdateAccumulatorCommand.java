package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.UpdateAccumulatorRequest;
import com.satnar.air.ucip.client.response.UpdateAccumulatorResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class UpdateAccumulatorCommand extends AbstractAirCommand<UpdateAccumulatorResponse> {

	private UpdateAccumulatorRequest request;
	
	public UpdateAccumulatorCommand(UpdateAccumulatorRequest request) {
		this.request = request;
	}

	@Override
	public UpdateAccumulatorResponse execute() throws UcipException {
		UpdateAccumulatorResponse response = new UpdateAccumulatorResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
}