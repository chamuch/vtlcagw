package com.satnar.air.ucip.client.command;

import java.util.List;

import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.internal.CsAirContext;
import com.satnar.air.ucip.client.request.GetFaFListRequest;
import com.satnar.air.ucip.client.response.FafInformationList;
import com.satnar.air.ucip.client.response.GetFaFListResponse;
import com.satnar.air.ucip.client.xmlrpc.XmlRpcException;


public class GetFaFListCommand extends AbstractAirCommand<GetFaFListResponse> {
	
	private GetFaFListRequest request;

	public GetFaFListCommand(GetFaFListRequest request) {
		System.out.println("setting getfaflist");
		this.request = request;
	}

	@Override
	public GetFaFListResponse execute() throws UcipException {
		GetFaFListResponse response = new GetFaFListResponse();
		try {
			CsAirContext.getAirClient().execute(request, response);
			System.out.println("FafChangeUnbarDate is:"  +response.getFafChangeUnbarDate());
			
			List<FafInformationList> res=response.getFafInformationList();
			System.out.println("response6 is:" +response.getResponseCode());
			for(FafInformationList flist: res)
			{	
							System.out.println("FafNumber:"+flist.getFafNumber());
							System.out.println("FafIndicator:"+flist.getFafIndicator());
							System.out.println("Owner:"+flist.getOwner());
							
			}
			Integer[] test=response.getNegotiatedCapabilities();
			for(Integer inttest:test)
			{			System.out.println("NegotiatedCapabilities is:" +inttest.intValue());
			}
			
			Integer[] test1=response.getAvailableServerCapabilities();
			for(Integer inttest:test1)
			{			System.out.println("AvailableServerCapabilities is:" +inttest.intValue());
			}
									
			
			
		} catch (XmlRpcException e) {
            throw new UcipException(e.code, e.getMessage(), e.linkedException);
		}
		return response;
	}
}


