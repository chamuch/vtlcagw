package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.UpdateBalanceAndDateRequest;
import com.satnar.air.ucip.client.response.UpdateBalanceAndDateResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;
import com.satnar.common.LogService;



public class UpdateBalanceAndDateCommand extends AbstractAirCommand<UpdateBalanceAndDateResponse> {
	
	private UpdateBalanceAndDateRequest request;

	public UpdateBalanceAndDateCommand(UpdateBalanceAndDateRequest request) {
		this.request = request;
	}

	@Override
	public UpdateBalanceAndDateResponse execute() throws UcipException {
		UpdateBalanceAndDateResponse response = new UpdateBalanceAndDateResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
			
			LogService.appLog.info("UpdateBalanceAndDateResponse-execute:Success:Msisdn:"+request.getSubscriberNumber());
		} catch (XmlRpcException e) {
			LogService.appLog.debug("UpdateBalanceAndDateResponse-execute:failed:Msisdn:"+request.getSubscriberNumber()+":ResponseCode:"+response.getResponseCode());
			throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}

}
