package com.ericsson.raso.cac.cagw.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.pps.diameter.dccapi.command.Cca;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.satnar.charging.diameter.ResultCode;
import com.satnar.common.LogService;

public class DccNotImplementedProcessor implements Processor {
	
	

	@Override
	public void process(Exchange exchange) throws Exception {
		//System.out.println("Entered into MMS_DCC::DIRECT_DEBIT ChargeAmountProcessor");
		LogService.appLog.debug("Entered into DccNotImplementedProcessor");
		
		try {
		    Ccr dccRequest = (Ccr) exchange.getIn().getBody();
	        Cca dccResponse = new Cca(dccRequest, ResultCode.DIAMETER_COMMAND_UNSUPPORTED.getCode());
	        
	        exchange.getOut().setBody(dccResponse);
	        
	        LogService.appLog.debug("DccNotImplementedProcessor-process:Unsupported:"+dccRequest.getCCRequestType());
		} catch (Exception e) {
            //TODO: Log for troubleshooting
			LogService.stackTraceLog.debug("DccNotImplementedProcessor-Unforeseen Failure for Unhandled Raquest!!:",e);
            throw new ServiceLogicException("Unforeseen Failure for Unhandled Raquest!!", e);
		}
		
		
		
	}

	
	
	

}
