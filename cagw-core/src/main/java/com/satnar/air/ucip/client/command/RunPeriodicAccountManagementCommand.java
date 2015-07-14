package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.RunPeriodicAccountManagementRequest;
import com.satnar.air.ucip.client.response.RunPeriodicAccountManagementResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class RunPeriodicAccountManagementCommand extends AbstractAirCommand<RunPeriodicAccountManagementResponse>{
	
	private RunPeriodicAccountManagementRequest request;
	
	public RunPeriodicAccountManagementCommand(RunPeriodicAccountManagementRequest request) {
		this.request = request;
	}
	
	@Override
	public RunPeriodicAccountManagementResponse execute() throws UcipException {
		RunPeriodicAccountManagementResponse response = new RunPeriodicAccountManagementResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
		} catch (XmlRpcException e) {
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return null;
	}

}
