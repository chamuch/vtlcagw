package com.ericsson.raso.cac.cagw.processor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownServiceException;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.pps.diameter.dccapi.avp.CCRequestNumberAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCRequestTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCServiceSpecificUnitsAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCTimeAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCTotalOctetsAvp;
import com.ericsson.pps.diameter.dccapi.avp.MultipleServicesCreditControlAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedActionAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedServiceUnitAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceIdentifierAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterInfoAvp;
import com.ericsson.pps.diameter.dccapi.avp.UsedServiceUnitAvp;
import com.ericsson.pps.diameter.dccapi.command.Cca;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.rfcapi.base.avp.AvpDataException;
import com.ericsson.pps.diameter.rfcapi.base.avp.EventTimestampAvp;
import com.ericsson.pps.diameter.rfcapi.base.avp.ResultCodeAvp;
import com.ericsson.pps.diameter.rfcapi.base.avp.SessionIdAvp;
import com.ericsson.pps.diameter.rfcapi.base.avp.avpdatatypes.Time;
import com.ericsson.pps.diameter.rfcapi.base.message.BadMessageException;
import com.ericsson.pps.diameter.rfcapi.base.message.NoRouteException;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdAvp;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdDataAvp;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdNatureAvp;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdTypeAvp;
import com.ericsson.pps.diameter.scapv2.avp.TimeZoneAvp;
import com.ericsson.pps.diameter.scapv2.avp.TrafficCaseAvp;
import com.ericsson.raso.cac.cagw.SpringHelper;
import com.satnar.charging.diameter.scap.client.ScapChargingEndpoint;
import com.satnar.common.LogService;
import com.satnar.common.charging.diameter.Peer;
import com.satnar.common.charging.diameter.ResultCode;



public class ChargeAmountProcessor implements Processor {

    
    private static int SCAP_SERVICE_IDENTIFIER = 8; //as per Mikael's inputs; no mail or written requirements though!!!
	
	@Override
	public void process(Exchange exchange) throws Exception {
	    MmsDccCharge response = null;
	    Cca scapResponse = null;
	    MmsDccCharge mmsRequest = (MmsDccCharge) exchange.getIn().getBody();
	    LogService.stackTraceLog.info("MMS DCC Request>> " + mmsRequest.toString());
        
		try {
		    LogService.appLog.debug("Preparing SCAP Request for MMS DCC Request# " + mmsRequest.getSessionId());
            Ccr scapRequest = this.getScapRequest(mmsRequest);
	        
            LogService.stackTraceLog.info("Sending SCAP CCR Request to OCC>> " + scapRequest.toString());
            scapResponse = scapRequest.send();
            LogService.stackTraceLog.info("Received SCAP CCA Response from OCC>> " + scapResponse.toString());
            
            response = this.getResponse(mmsRequest, scapResponse, scapResponse.getResultCode());
            
            LogService.stackTraceLog.info("MMS DCC Response>> " + response.toString());
            exchange.getOut().setBody(response);
	        
	        LogService.appLog.debug("ChargeAmountProcessor-process:Done.SessionId:"+scapRequest.getSessionId()
	        		+":ResultCode"+scapResponse.getResultCode());
		} catch (NoRouteException e) {
			LogService.appLog.debug("ChargeAmountProcessor-process:No Diamter Peer available based on the Stack Config & Request Parameters!!",e);
			mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
			LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            return;
		} catch (BadMessageException e) {
			LogService.appLog.debug("ChargeAmountProcessor-process:Request rejected by OCC/CCN!!",e);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            return;
		} catch (URISyntaxException e) {
			LogService.appLog.debug("ChargeAmountProcessor-process:Bad Peer configuration for the route!!",e);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            return;
		} catch (UnknownServiceException e) {
			LogService.appLog.debug("ChargeAmountProcessor-process:Bad Stack Configuration - Service!!",e);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            return;
		} catch (AvpDataException e) {
			LogService.appLog.debug("ChargeAmountProcessor-process:Presentation Tier Failure for Raquest Parameter!!",e);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            return;
		} catch (IOException e) {
			LogService.appLog.debug("ChargeAmountProcessor-process:Transport Tier Failure for Raquest!!",e);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            return;
		} catch (Exception genE){//Added for debugging
			LogService.appLog.debug("ChargeAmountProcessor-process:Transport Tier Failure for Raquest!!",genE);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            return;
		} catch (Error genE){//Added for debugging
            LogService.appLog.debug("ChargeAmountProcessor-process:Transport Tier Failure for Raquest!!",genE);
            mmsRequest = this.getFailedResponse(mmsRequest, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
            LogService.stackTraceLog.info("MMS DCC Response>> " + mmsRequest.toString());
            exchange.getOut().setBody(mmsRequest);
            return;
        }
				
		LogService.appLog.info("Exiting from DIRECT_DEBIT ChargeAmountProcessor");
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

    private MmsDccCharge getResponse(MmsDccCharge mmsRequest, Cca scapResponse, Long resultCode) {
        MmsDccCharge response = new MmsDccCharge();
        try {
            response.setResultCode(new ResultCodeAvp(resultCode));
            response.addAvp(new CCRequestNumberAvp(scapResponse.getCCRequestNumber()));
            response.addAvp(new CCRequestTypeAvp(scapResponse.getCCRequestType()));
            response.addAvp(new SessionIdAvp(scapResponse.getSessionId()));

            //TODO: SCAP Response ==> (Granted Units, Cost Info)
            //TODO: DCC Response -- need to add params based on customer inputs, during integration testing.

        } catch (AvpDataException e) {
            LogService.appLog.error("Unable to read from SCAP DIAMETER Message. Returning error respose");
            response.setResultCode(new ResultCodeAvp(ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode()));
        } catch (Error e) {
            LogService.appLog.error("Unable to read from SCAP DIAMETER Message. Returning error respose");
            response.setResultCode(new ResultCodeAvp(ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode()));
        }
        return response;
    }

    private Ccr getScapRequest(MmsDccCharge mmsDccChargeRequest) throws ServiceLogicException {
	    Ccr scapCcr = null;
	    ScapChargingEndpoint scapStack = (ScapChargingEndpoint) SpringHelper.getScapDiameter();
	    try {
	        ScapChargingEndpoint scapEndpoint = (ScapChargingEndpoint) SpringHelper.getScapDiameter();
            LogService.appLog.debug("SCAP Endpoint as configured in CAMEL available: " + (scapEndpoint != null));
            
            if(scapEndpoint == null) {
                LogService.appLog.error("SCAP Endpoint instance could not be acquired!!");
                throw new ServiceLogicException("Backend (SCAP Endpoint) not available for processing this request# " + mmsDccChargeRequest.getSessionId());
            }
            
	    	scapCcr = new Ccr(mmsDccChargeRequest.getSessionId(), scapStack.getDccStack().getDiameterStack(), ChargingHelper.SERVICE_CONTEXT_ID);	    	
	    	LogService.appLog.debug("DCC CCR (SCAP Variant) created for request# " + mmsDccChargeRequest.getSessionId()); 
            
	    	
	    	Peer route = scapStack.getScapLoadBalancer().getPeerBySite("1");
	    	scapCcr.setDestinationRealm(route.getRealm());	    	
	    	scapCcr.setCCRequestNumber(0x00);  // DCC::DIRECT_DEBIT
	    	scapCcr.setCCRequestType(CCRequestTypeAvp.EVENT_REQUEST); 
	    	scapCcr.setRequestedAction(RequestedActionAvp.DIRECT_DEBITING); 
            
	    	
	    	
	    	// MSSC
	    	if (mmsDccChargeRequest.getAvp(455) != null) { // MSCC Indicator
	    	    LogService.appLog.debug("MSCC Indicator available.. Will process RSU for SCAP...");
	    	    //TODO: MSSCC Ind to be confirmed by Per
	    		//scapCcr.addAvp(new MultipleServicesIndicatorAvp(dccRequest.getMultipleServicesIndicator())); 
	    	}
	    	
	    	// viettel specific RSU Processing (as instructed by Per)
            {
	    	    RequestedServiceUnitAvp rsuAvp = new RequestedServiceUnitAvp();
	    	    
//	    	    Avp siAvp = mmsDccChargeRequest.getAvp(ZteDccHelper.ZTE_SERVICE_INFORMATION);
//	    	    if (siAvp == null) {
//	    	        LogService.appLog.error(String.format("Cannot find ZTE Service Infor (avpCode: %s) in mms dcc charge request", ZteDccHelper.ZTE_SERVICE_INFORMATION));
//	    	        throw new ServiceLogicException(String.format("Cannot find MSCCArray(avpCode: %s) in mms dcc charge request", ZteDccHelper.ZTE_SERVICE_INFORMATION));
//	    	    }
	    	    
//	    	    Avp msccAvp = ZteDccHelper.getSubAvp(siAvp, 456);
	    	    Avp msccAvp = mmsDccChargeRequest.getAvp(456);
                if (msccAvp == null) {
	    	        LogService.appLog.error(String.format("Cannot find MSCCArray(avpCode: %s) in mms dcc charge request", MultipleServicesCreditControlAvp.AVP_CODE));
	    	        throw new ServiceLogicException(String.format("Cannot find MSCCArray(avpCode: %s) in mms dcc charge request", MultipleServicesCreditControlAvp.AVP_CODE));
	    	    }
	    	    
	    	    Avp usuAvp = ZteDccHelper.getSubAvp(msccAvp, 446); // ZTE Used Service Unit
	    	    if (usuAvp == null) {
	    	        LogService.appLog.error(String.format("Cannot find UsedServiceUnitAvp(avpCode: %s) in mms dcc charge request", UsedServiceUnitAvp.AVP_CODE));
	    	        throw new ServiceLogicException(String.format("Cannot find UsedServiceUnitAvp(avpCode: %s) in mms dcc charge request", UsedServiceUnitAvp.AVP_CODE));
	    	    }
	    	    
	    	    
	    	    Avp cctoAvp = ZteDccHelper.getSubAvp(usuAvp, CCTotalOctetsAvp.AVP_CODE);
	    	    if (cctoAvp != null) {
	    	        LogService.appLog.debug("Found CCTotalOctets under ZTE MSCC RSU with value: " + cctoAvp.getAsLong());
	    	        scapCcr.setRequestedServiceUnit(cctoAvp);
	    	    } else {
	    	        Avp cctAvp = ZteDccHelper.getSubAvp(usuAvp, CCTimeAvp.AVP_CODE);
	    	        if (cctAvp != null) {
	    	            LogService.appLog.debug("Found CCTime under ZTE MSCC RSU with value: " + cctAvp.getAsLong());
	    	            scapCcr.setRequestedServiceUnit(cctAvp);
	    	        } else {
	    	            Avp ccssuAvp = ZteDccHelper.getSubAvp(usuAvp, CCServiceSpecificUnitsAvp.AVP_CODE);
	    	            if (ccssuAvp != null) {
	    	                LogService.appLog.debug("Found CCServiceSpecificUnits under ZTE MSCC RSU with value: " + ccssuAvp.getAsLong());
	    	                scapCcr.setRequestedServiceUnit(ccssuAvp);
	    	            } else {
	    	                LogService.appLog.error("Unable to find Time, Octets or SSU units in RSU. Bad Request from ZTE!!");
	    	                throw new ServiceLogicException("Unable to find Time, Octets or SSU units in RSU. Bad Request from ZTE!!");
	    	            }
	    	        }
	    	    }
	    	}
	    	    
	    	    
	    	// Traffic case
	    	int trafficCase = ZteDccHelper.getTrafficCase(mmsDccChargeRequest);
	    	LogService.appLog.debug("traffic Case from Zte Request: " + trafficCase);
	    	if (trafficCase == 2) {
	    		scapCcr.addAvp(new TrafficCaseAvp(21));
	    		LogService.appLog.debug("SCAP traffic Case set to MO, value: 21");
	            	
	    		Avp osiAvp = ZteDccHelper.getOaSubscriptionId(mmsDccChargeRequest);
	    		
	    		String aParty = ZteDccHelper.getOaSubscriptionIdData(mmsDccChargeRequest);
	    		int aPartyType = ZteDccHelper.getOaSubscriptionIdType(mmsDccChargeRequest);
	    		OtherPartyIdTypeAvp opitAvp = new OtherPartyIdTypeAvp();
	    		opitAvp.setData(aPartyType);
	    		OtherPartyIdDataAvp opidAvp = new OtherPartyIdDataAvp();
	    		opidAvp.setData(aParty);
	    		LogService.appLog.debug("A-Party number from Zte Request: " + aParty + ", type: " + aPartyType);
	    		
	    		OtherPartyIdAvp otherPartyIdAvp = new OtherPartyIdAvp();
	    		otherPartyIdAvp.addSubAvp(opitAvp);
	    		otherPartyIdAvp.addSubAvp(opidAvp);
	    		otherPartyIdAvp.addSubAvp(new OtherPartyIdNatureAvp(0));
	    		
	    		scapCcr.addAvp(otherPartyIdAvp);
	    		scapCcr.addSubscriptionId(aParty, aPartyType);
	    		LogService.appLog.debug("SubscriptionId added: " + aParty + ", type: " + aPartyType);
                
	    	} else {
	    		scapCcr.addAvp(new TrafficCaseAvp(20));
	    		LogService.appLog.debug("SCAP traffic Case set to MT, value: 20");
                
	    		Avp osiAvp = ZteDccHelper.getOaSubscriptionId(mmsDccChargeRequest);
	    		
	    		String bParty = ZteDccHelper.getDaSubscriptionIdData(mmsDccChargeRequest);
	    		int bPartyType = ZteDccHelper.getDaSubscriptionIdType(mmsDccChargeRequest);
	    		OtherPartyIdTypeAvp opitAvp = new OtherPartyIdTypeAvp();
	    		opitAvp.setData(bPartyType);
	    		OtherPartyIdDataAvp opidAvp = new OtherPartyIdDataAvp();
	    		opidAvp.setData(bParty);
	    		LogService.appLog.debug("A-Party number from Zte Request: " + bParty + ", type: " + bPartyType);
                
	    		OtherPartyIdAvp otherPartyIdAvp = new OtherPartyIdAvp();
	    		otherPartyIdAvp.addSubAvp(opitAvp);
	    		otherPartyIdAvp.addSubAvp(opidAvp);
	    		otherPartyIdAvp.addSubAvp(new OtherPartyIdNatureAvp(0));

	    		scapCcr.addAvp(otherPartyIdAvp);
	    		scapCcr.addSubscriptionId(bParty, bPartyType);
                LogService.appLog.debug("SubscriptionId added: " + bParty + ", type: " + bPartyType);
	    	}
	    	
	    	// service identifier
	    	scapCcr.addAvp(new ServiceIdentifierAvp(SCAP_SERVICE_IDENTIFIER));
	    	LogService.appLog.debug("ServiceIdentifier:"+SCAP_SERVICE_IDENTIFIER);
	    	
	    	// SPI - Service Enabler Type
	    	String serviceEnablerType = Integer.toHexString(ZteDccHelper.getServiceEnablerType(mmsDccChargeRequest));
	    	ServiceParameterInfoAvp spiAvp = ChargingHelper.createSPI(100, serviceEnablerType);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(100) Service Enabler Type added:" + serviceEnablerType);
            	    	
	    	// SPI - Message ID
	    	int messageId = ZteDccHelper.getMessageId(mmsDccChargeRequest);
	    	spiAvp = ChargingHelper.createSPI(500, messageId);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(500) Message ID added:" + messageId);
            	    	
	    	// SPI - OA Subscription ID Type
	    	int aPartyType = ZteDccHelper.getOaSubscriptionIdType(mmsDccChargeRequest);
	    	spiAvp = ChargingHelper.createSPI(300, aPartyType);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(300) A-Party Type added:" + aPartyType);
            
	    	// SPI - OA Subscription ID Data
	    	String aParty = ZteDccHelper.getOaSubscriptionIdData(mmsDccChargeRequest);
	    	spiAvp = ChargingHelper.createSPI(301, aParty);
	    	scapCcr.addAvp(spiAvp);
            LogService.appLog.debug("SPI(301) A-Party added:" + aParty);
	    	
	    	// SPI - DA Subscription ID Type
            int bPartyType = ZteDccHelper.getDaSubscriptionIdType(mmsDccChargeRequest);
	    	spiAvp = ChargingHelper.createSPI(302, bPartyType);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(302) B-Party Type added:" + bPartyType);
            
	    	// SPI - DA Subscription ID Data
	    	String bParty = ZteDccHelper.getDaSubscriptionIdData(mmsDccChargeRequest);
            spiAvp = ChargingHelper.createSPI(303, bParty);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(303) B-Party added:" + aParty);
            
	    	// SPI - SP ID
	    	String spId = ZteDccHelper.getSpId(mmsDccChargeRequest);
            spiAvp = ChargingHelper.createSPI(501, spId);
            scapCcr.addAvp(spiAvp);
            LogService.appLog.debug("SPI(501) SP-ID added:" + spId);
            
            //SPI - ISMP-Info-ChargingType
            String chargingType = ZteDccHelper.getChargingType(mmsDccChargeRequest);
            spiAvp = ChargingHelper.createSPI(101, ZteDccHelper.getChargingType(mmsDccChargeRequest));
            scapCcr.addAvp(spiAvp);
            LogService.appLog.debug("SPI(101) Charging Type added:" + chargingType);
            
            
            // Event-Timestamp
            Time timestamp = new Time(new Date(System.currentTimeMillis()));
            scapCcr.addAvp(new EventTimestampAvp(timestamp));
            LogService.appLog.debug("Timestamp added:" + timestamp);
            	
	    	// Timezone
            scapCcr.addAvp(new TimeZoneAvp((byte)10, (byte)0, (byte)0));
            LogService.appLog.debug("Timezone added: 10h 0mins 0dst");
            
           
            LogService.stackTraceLog.debug("ChargeAmountProcessor-getScapRequest:final construction:"+scapCcr.toString());
	    } catch (AvpDataException e) {
	    	//TODO: Log for troubleshooting
	    	LogService.stackTraceLog.debug("ChargeAmountProcessor-getScapRequest:Direct Debit Use Failed!",e);
	    	throw new ServiceLogicException("Direct Debit Use Failed!", e);
	    }
	    LogService.appLog.info("Exiting from DIRECT_DEBIT ChargeAmountProcessor");
		return scapCcr;
    }
}
