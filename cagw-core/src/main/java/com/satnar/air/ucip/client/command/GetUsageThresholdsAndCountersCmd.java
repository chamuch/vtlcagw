package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.GetUsageThresholdsAndCountersRequest;
import com.satnar.air.ucip.client.response.GetUsageThresholdsAndCountersResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;



public class GetUsageThresholdsAndCountersCmd extends AbstractAirCommand<GetUsageThresholdsAndCountersResponse>{
	
	private GetUsageThresholdsAndCountersRequest request;
	
	public GetUsageThresholdsAndCountersCmd(GetUsageThresholdsAndCountersRequest request)
	{
		this.request = request;	
	}

	@Override
	public GetUsageThresholdsAndCountersResponse execute() throws UcipException {
		GetUsageThresholdsAndCountersResponse response = new GetUsageThresholdsAndCountersResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}

}
