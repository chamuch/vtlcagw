package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.AddPeriodicAccountManagementDataReq;
import com.satnar.air.ucip.client.response.AddPeriodicAccountManagementDataRes;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class AddPeriodicAccountManagementDataCmd extends AbstractAirCommand<AddPeriodicAccountManagementDataRes> {

	private AddPeriodicAccountManagementDataReq request;
	
	public AddPeriodicAccountManagementDataCmd(AddPeriodicAccountManagementDataReq request) {
		this.request = request;
	}

	@Override
	public AddPeriodicAccountManagementDataRes execute() throws UcipException {
		AddPeriodicAccountManagementDataRes response = new AddPeriodicAccountManagementDataRes();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
			throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return null;
	}

}
