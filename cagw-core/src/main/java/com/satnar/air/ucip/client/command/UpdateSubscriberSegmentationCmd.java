package com.satnar.air.ucip.client.command;

import java.util.List;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.UpdateSubscriberSegmentRequest;
import com.satnar.air.ucip.client.response.SegmentationResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class UpdateSubscriberSegmentationCmd extends AbstractAirCommand<SegmentationResponse> {
	
	private UpdateSubscriberSegmentRequest request;
	
	public UpdateSubscriberSegmentationCmd(UpdateSubscriberSegmentRequest request) {
		this.request = request;
	}

	@Override
	public SegmentationResponse execute() throws UcipException {
		SegmentationResponse response = new SegmentationResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
			throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
	
	
	
}
