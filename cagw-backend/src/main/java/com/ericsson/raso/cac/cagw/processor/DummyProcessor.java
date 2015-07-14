package com.ericsson.raso.cac.cagw.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.pps.diameter.dccapi.avp.CCRequestTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedActionAvp;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotify;
import com.satnar.common.LogService;
import com.satnar.smpp.pdu.SmppPdu;

public class DummyProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
    	
    	LogService.appLog.debug("BackendFacadeProcessor-process:Entered");
        Object request = exchange.getIn().getBody();
        
        if(request != null){
        	LogService.appLog.debug("Object is not empty..");
        }
    }
    
}
