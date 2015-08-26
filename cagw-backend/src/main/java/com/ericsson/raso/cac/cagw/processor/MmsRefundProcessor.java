package com.ericsson.raso.cac.cagw.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.pps.diameter.dccapi.avp.CCRequestNumberAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCRequestTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdAvp;
import com.ericsson.pps.diameter.rfcapi.base.avp.ResultCodeAvp;
import com.ericsson.pps.diameter.rfcapi.base.avp.SessionIdAvp;
import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.command.RefillCommand;
import com.satnar.air.ucip.client.request.RefillRequest;
import com.satnar.air.ucip.client.response.RefillResponse;
import com.satnar.common.LogService;
import com.satnar.common.charging.diameter.ResultCode;



public class MmsRefundProcessor implements Processor {

    @Override
	public void process(Exchange exchange) throws Exception {
	    MmsDccCharge response = null;
	    MmsDccCharge mmsRequest = (MmsDccCharge) exchange.getIn().getBody();
	    LogService.stackTraceLog.info("MMS DCC Request>> " + mmsRequest.toString());
        
		try {
		    LogService.appLog.debug("Preparing SCAP Request for MMS DCC Request# " + mmsRequest.getSessionId());
            
          //TODO: MMS Refund semantic parameter mapping starts here...
            RefillRequest refillRequest = new RefillRequest();
            
            SubscriptionIdAvp subscriberId = (SubscriptionIdAvp) mmsRequest.getAvp(SubscriptionIdAvp.AVP_CODE);
            String subscriberNumber = subscriberId.getSubscriptionIdData();
            refillRequest.setSubscriberNumber(subscriberNumber);
            
            refillRequest.setExternalData1(mmsRequest.getSessionId());   // mapped for mms refund as per Imtiaz inputs
            
            refillRequest.setRefProfID("MMSR");       // hard-coded for mms refund refill defined
            refillRequest.setTransacAmount("0");      // hard-coded for mms refund as per Imtiaz inputs
            refillRequest.setTransacCurrency("VND");  // hard-coded for mms refund as per Imtiaz inputs
            
	        
            LogService.stackTraceLog.info("Sending REFILL Request to OCC>> " + refillRequest.toString());
            RefillResponse refillResponse = new RefillCommand(refillRequest).execute();
            LogService.stackTraceLog.info("Received REFILL Response from OCC>> " + refillRequest.toString());
            
            response = this.getResponse(mmsRequest, refillResponse, refillResponse.getResponseCode());
            
            LogService.stackTraceLog.info("MMS DCC Response>> " + response.toString());
            exchange.getOut().setBody(response);
            
            LogService.appLog.info("MMS DCC Response Success - sessionId: " + mmsRequest.getSessionId());
		} catch (UcipException e) {
            LogService.appLog.debug("ChargeAmountProcessor-process:Ucip Failure for Raquest!!",e);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            LogService.appLog.info("MMS DCC Response Failed - sessionId: " + mmsRequest.getSessionId());
            return;
        } catch (Exception genE){//Added for debugging
			LogService.appLog.debug("ChargeAmountProcessor-process:CatchAll Failure for Raquest!!",genE);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            LogService.appLog.info("MMS DCC Response Failed - sessionId: " + mmsRequest.getSessionId());
            return;
		} catch (Error genE){//Added for debugging
            LogService.appLog.debug("ChargeAmountProcessor-process:CatchAll Error for Raquest!!",genE);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            LogService.appLog.info("MMS DCC Response Failed - sessionId: " + mmsRequest.getSessionId());
            return;
        }
    }

	private MmsDccCharge getFailedResponse(MmsDccCharge mmsRequest, int code) {
	        try {
	            mmsRequest.setResultCode(new ResultCodeAvp(code));
	           
	        } catch (Error e) {
	            LogService.appLog.error("Unable to read from SCAP DIAMETER Message. Returning error respose");
	            mmsRequest.setResultCode(new ResultCodeAvp(ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode()));
	        }
	        return mmsRequest;
	}

    private MmsDccCharge getResponse(MmsDccCharge mmsRequest, RefillResponse refillResponse, int resultCode) {
        MmsDccCharge response = new MmsDccCharge();
        try {
            switch (resultCode) {
                case 0:
                case 1:
                case 2:
                    response.setResultCode(new ResultCodeAvp(2001));    // hard-coded for mms refund as per Imtiaz inputs
                default:
                    response.setResultCode(new ResultCodeAvp(4010));    // hard-coded for mms refund as per Imtiaz inputs
            }

            response.addAvp(mmsRequest.getAvp(CCRequestNumberAvp.AVP_CODE));
            response.addAvp(mmsRequest.getAvp(CCRequestTypeAvp.AVP_CODE));
            response.addAvp(mmsRequest.getAvp(SessionIdAvp.AVP_CODE));

            //TODO: SCAP Response ==> (Granted Units, Cost Info)
            //TODO: DCC Response -- need to add params based on customer inputs, during integration testing.

        } catch (Error e) {
            LogService.appLog.error("Unable to read from Refill Response Message. Returning error respose");
            response.setResultCode(new ResultCodeAvp(ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode()));
        }
        return response;
    }

}
