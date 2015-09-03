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

public class BackendFacadeProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
    	
    	LogService.appLog.debug("BackendFacadeProcessor-process:Entered");
        Object request = exchange.getIn().getBody();
        
        Processor delegate = null;
        
        if (request instanceof SmppPdu) {
            if (request instanceof AuthAcc) {
                delegate = Usecase.AUTH_CC.getProcessor();
            }
            
            if (request instanceof SmResultNotify) {
                delegate = Usecase.SM_RESULT_NOTIFY.getProcessor();
            }
        }
        
        if (request instanceof MmsDccCharge) {
            MmsDccCharge dccRequest = (MmsDccCharge) request;
            LogService.appLog.info("SessionId:"+dccRequest.getSessionId() + ":RequestedAction:"+dccRequest.getRequestedAction());
            /*if (dccRequest.getAvp(CCRequestTypeAvp.AVP_CODE).getAsInt() == CCRequestTypeAvp.EVENT_REQUEST ||
                    dccRequest.getAvp(RequestedActionAvp.AVP_CODE).getAsInt() == RequestedActionAvp.DIRECT_DEBITING) {
                delegate = Usecase.DIRECT_DEBIT.getProcessor();
            } else if (dccRequest.getAvp(CCRequestTypeAvp.AVP_CODE).getAsInt() == CCRequestTypeAvp.EVENT_REQUEST ||
                    dccRequest.getAvp(RequestedActionAvp.AVP_CODE).getAsInt() == RequestedActionAvp.REFUND_ACCOUNT) {
                delegate = Usecase.REFUND.getProcessor();
            } else {
                delegate = new DccNotImplementedProcessor();
            }*/
            
            if (dccRequest.getAvp(CCRequestTypeAvp.AVP_CODE).getAsInt() == CCRequestTypeAvp.EVENT_REQUEST){
            	if(dccRequest.getAvp(RequestedActionAvp.AVP_CODE).getAsInt() == RequestedActionAvp.DIRECT_DEBITING){
            		delegate = Usecase.DIRECT_DEBIT.getProcessor();
            	}else if(dccRequest.getAvp(RequestedActionAvp.AVP_CODE).getAsInt() == RequestedActionAvp.REFUND_ACCOUNT){
            		delegate = Usecase.REFUND.getProcessor();
            	}else {
                    delegate = new DccNotImplementedProcessor();
                }
            }else {
                delegate = new DccNotImplementedProcessor();
            }
        }
        
        if (delegate == null) 
            throw new ServiceLogicException("Usecase not identified OR delegation refused");
        
        delegate.process(exchange);
        
    }
    
}
