package com.ericsson.raso.cac.cagw.processor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownServiceException;
import java.security.Timestamp;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.pps.diameter.dccapi.avp.CCRequestNumberAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCRequestTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCServiceSpecificUnitsAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCTimeAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCTotalOctetsAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedActionAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedServiceUnitAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceIdentifierAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterInfoAvp;
import com.ericsson.pps.diameter.dccapi.avp.UsedServiceUnitAvp;
import com.ericsson.pps.diameter.dccapi.avp.avpdatatypes.DccGrouped;
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

    private Ccr getScapRequest(MmsDccCharge dccRequest) throws ServiceLogicException {
	    Ccr scapCcr = null;
	    ScapChargingEndpoint scapStack = (ScapChargingEndpoint) SpringHelper.getScapDiameter();
	    try {
	        ScapChargingEndpoint scapEndpoint = (ScapChargingEndpoint) SpringHelper.getScapDiameter();
            LogService.appLog.debug("SCAP Endpoint as configured in CAMEL available: " + (scapEndpoint != null));
            
            if(scapEndpoint == null) {
                LogService.appLog.error("SCAP Endpoint instance could not be acquired!!");
                throw new ServiceLogicException("Backend (SCAP Endpoint) not available for processing this request# " + dccRequest.getSessionId());
            }
            
	    	scapCcr = new Ccr(dccRequest.getSessionId(), scapStack.getDccStack().getDiameterStack(), ChargingHelper.SERVICE_CONTEXT_ID);	    	
	    	LogService.appLog.debug("DCC CCR (SCAP Variant) created for request# " + dccRequest.getSessionId()); 
            
	    	
	    	Peer route = scapStack.getScapLoadBalancer().getPeerBySite("1");
	    	scapCcr.setDestinationRealm(route.getRealm());	    	
	    	scapCcr.setCCRequestNumber(0x00);  // DCC::DIRECT_DEBIT
	    	scapCcr.setCCRequestType(CCRequestTypeAvp.EVENT_REQUEST); 
	    	scapCcr.setRequestedAction(RequestedActionAvp.DIRECT_DEBITING); 
            
	    	
	    	
	    	// MSSC
	    	if (dccRequest.getAvp(455) != null) { // MSCC Indicator
	    		//TODO: MSSCC Ind to be confirmed by Per
	    		//scapCcr.addAvp(new MultipleServicesIndicatorAvp(dccRequest.getMultipleServicesIndicator())); 
	    		
	    		// viettel specific (as instructed by Per)
	    		RequestedServiceUnitAvp rsuAvp = new RequestedServiceUnitAvp();
	    		
	    		for (Avp msccAvp: dccRequest.getAvp(456).getDataAsGroup()) { // MSCC Array Avp
	    			for (Avp usuAvp: (msccAvp).getDataAsGroup()) {
	    				if (usuAvp.getAvpCode() == UsedServiceUnitAvp.AVP_CODE) {
	    					for (Avp requestedUnits: ((DccGrouped)usuAvp).getValues()) {
	    						// handle cc-total-octets
	    						if (requestedUnits.getAvpCode() == CCTotalOctetsAvp.AVP_CODE) {
	    							CCTotalOctetsAvp cctoAvp = new CCTotalOctetsAvp();
	    							cctoAvp.setData(((CCTotalOctetsAvp)requestedUnits).getValueAsLong());
                                    scapCcr.setRequestedServiceUnit(cctoAvp);
	    							LogService.appLog.debug(":CCTotalOctets:"+((CCTotalOctetsAvp)requestedUnits).getValueAsLong());
	    						}
	    						
	    						// handle cc-time
	    						if (requestedUnits.getAvpCode() == CCTimeAvp.AVP_CODE) {
	    							CCTimeAvp cctAvp = new CCTimeAvp();
	    							cctAvp.setData(((CCTimeAvp)requestedUnits).getValue());
                                    scapCcr.setRequestedServiceUnit(cctAvp);
	    							LogService.appLog.debug(":CCTime:"+((CCTimeAvp)requestedUnits).getValue());
	    						}
	    						
	    						// handle cc-service-specific-units
	    						if (requestedUnits.getAvpCode() == CCServiceSpecificUnitsAvp.AVP_CODE) {
	    							CCServiceSpecificUnitsAvp ccssuAvp = new CCServiceSpecificUnitsAvp();
	    							ccssuAvp.setData(((CCServiceSpecificUnitsAvp)requestedUnits).getValueAsLong());
	    							scapCcr.setRequestedServiceUnit(ccssuAvp);
	    							LogService.appLog.debug(":CCServiceSpecificUnit:"+((CCServiceSpecificUnitsAvp)requestedUnits).getValueAsLong());
	    						}
	    					}
	    				}
	    			}
	    		}
	    	}
	    	
	    	// Traffic case
	    	int trafficCase = ZteDccHelper.getTrafficCase(dccRequest);
	    	LogService.appLog.debug("traffic Case from Zte Request: " + trafficCase);
	    	if (trafficCase == 2) {
	    		scapCcr.addAvp(new TrafficCaseAvp(21));
	    		LogService.appLog.debug("SCAP traffic Case set to MO, value: 21");
	            	
	    		Avp osiAvp = ZteDccHelper.getOaSubscriptionId(dccRequest);
	    		
	    		String aParty = ZteDccHelper.getOaSubscriptionIdData(dccRequest);
	    		int aPartyType = ZteDccHelper.getOaSubscriptionIdType(dccRequest);
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
                
	    		Avp osiAvp = ZteDccHelper.getOaSubscriptionId(dccRequest);
	    		
	    		String bParty = ZteDccHelper.getDaSubscriptionIdData(dccRequest);
	    		int bPartyType = ZteDccHelper.getDaSubscriptionIdType(dccRequest);
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
	    	String serviceEnablerType = Integer.toHexString(ZteDccHelper.getServiceEnablerType(dccRequest));
	    	ServiceParameterInfoAvp spiAvp = ChargingHelper.createSPI(100, serviceEnablerType);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(100) Service Enabler Type added:" + serviceEnablerType);
            	    	
	    	// SPI - Message ID
	    	int messageId = ZteDccHelper.getMessageId(dccRequest);
	    	spiAvp = ChargingHelper.createSPI(500, messageId);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(500) Message ID added:" + messageId);
            	    	
	    	// SPI - OA Subscription ID Type
	    	int aPartyType = ZteDccHelper.getOaSubscriptionIdType(dccRequest);
	    	spiAvp = ChargingHelper.createSPI(300, aPartyType);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(300) A-Party Type added:" + aPartyType);
            
	    	// SPI - OA Subscription ID Data
	    	String aParty = ZteDccHelper.getOaSubscriptionIdData(dccRequest);
	    	spiAvp = ChargingHelper.createSPI(301, aParty);
	    	scapCcr.addAvp(spiAvp);
            LogService.appLog.debug("SPI(301) A-Party added:" + aParty);
	    	
	    	// SPI - DA Subscription ID Type
            int bPartyType = ZteDccHelper.getDaSubscriptionIdType(dccRequest);
	    	spiAvp = ChargingHelper.createSPI(302, bPartyType);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(302) B-Party Type added:" + bPartyType);
            
	    	// SPI - DA Subscription ID Data
	    	String bParty = ZteDccHelper.getDaSubscriptionIdData(dccRequest);
            spiAvp = ChargingHelper.createSPI(303, bParty);
	    	scapCcr.addAvp(spiAvp);
	    	LogService.appLog.debug("SPI(303) B-Party added:" + aParty);
            
	    	// SPI - SP ID
	    	String spId = ZteDccHelper.getSpId(dccRequest);
            spiAvp = ChargingHelper.createSPI(501, spId);
            scapCcr.addAvp(spiAvp);
            LogService.appLog.debug("SPI(501) SP-ID added:" + spId);
            
            //SPI - ISMP-Info-ChargingType
            String chargingType = ZteDccHelper.getChargingType(dccRequest);
            spiAvp = ChargingHelper.createSPI(101, ZteDccHelper.getChargingType(dccRequest));
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
