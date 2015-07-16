package com.ericsson.raso.cac.cagw.processor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownServiceException;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.pps.diameter.dccapi.avp.CCRequestNumberAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCServiceSpecificUnitsAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCTimeAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCTotalOctetsAvp;
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



public class ChargeAmountProcessor implements Processor {

    
    private static int SCAP_SERVICE_IDENTIFIER = 8; //as per Mikael's inputs; no mail or written requirements though!!!
	

	@Override
	public void process(Exchange exchange) throws Exception {
		//System.out.println("Entered into MMS_DCC::DIRECT_DEBIT ChargeAmountProcessor");
		LogService.appLog.info("Entered into DIRECT_DEBIT ChargeAmountProcessor");
		
		try {
		    Ccr dccRequest = (Ccr) exchange.getIn().getBody();
	        Ccr scapRequest = this.getScapRequest(dccRequest);
	        
	        Cca scapResponse = scapRequest.send();
	        Cca dccResponse = new Cca(dccRequest, scapResponse.getResultCode());
	        //TODO: SCAP Response ==> (Granted Units, Cost Info)
	        //TODO: DCC Response -- need to add params based on customer inputs, during integration testing.
	        
	        exchange.getOut().setBody(dccResponse);
	        
	        LogService.appLog.debug("ChargeAmountProcessor-process:Done.SessionId:"+scapRequest.getSessionId()
	        		+":ResultCode"+scapResponse.getResultCode());
		} catch (NoRouteException e) {
		    //TODO: Log for troubleshooting
			LogService.appLog.debug("ChargeAmountProcessor-process:No Diamter Peer available based on the Stack Config & Request Parameters!!",e);
		    throw new ServiceLogicException("No Diamter Peer available based on the Stack Config & Request Parameters!!", e);
		} catch (BadMessageException e) {
            //TODO: Log for troubleshooting
			LogService.appLog.debug("ChargeAmountProcessor-process:Request rejected by OCC/CCN!!",e);
            throw new ServiceLogicException("Request rejected by OCC/CCN!!", e);
		} catch (URISyntaxException e) {
            //TODO: Log for troubleshooting
			LogService.appLog.debug("ChargeAmountProcessor-process:Bad Peer configuration for the route!!",e);
            throw new ServiceLogicException("Bad Peer configuration for the route!!", e);
		} catch (UnknownServiceException e) {
            //TODO: Log for troubleshooting
			LogService.appLog.debug("ChargeAmountProcessor-process:Bad Stack Configuration - Service!!",e);
			throw new ServiceLogicException("Bad Stack Configuration - Service!!", e);
		} catch (AvpDataException e) {
            //TODO: Log for troubleshooting
			LogService.appLog.debug("ChargeAmountProcessor-process:Presentation Tier Failure for Raquest Parameter!!",e);
			throw new ServiceLogicException("Presentation Tier Failure for Raquest Parameter!!", e);
		} catch (IOException e) {
            //TODO: Log for troubleshooting
			LogService.appLog.debug("ChargeAmountProcessor-process:Transport Tier Failure for Raquest!!",e);
            throw new ServiceLogicException("Transport Tier Failure for Raquest!!", e);
		} catch (Exception genE){//Added for debugging
			LogService.appLog.debug("ChargeAmountProcessor-process:Transport Tier Failure for Raquest!!",genE);
            throw genE;
		}
				
		LogService.appLog.info("Exiting from DIRECT_DEBIT ChargeAmountProcessor");
	}

	private Ccr getScapRequest(Ccr dccRequest) throws ServiceLogicException {
	    Ccr scapCcr = null;
	    LogService.appLog.info("Entered into ChargeAmountProcessor-getScapRequest.");
	    ScapChargingEndpoint scapStack = (ScapChargingEndpoint) SpringHelper.getScapDiameter();
	    StringBuilder logMsg = null;
	    try {
	    	scapCcr = new Ccr(dccRequest.getSessionId(), scapStack.getDccStack().getDiameterStack(), ChargingHelper.SERVICE_CONTEXT_ID);	    	
	    	Peer route = scapStack.getScapLoadBalancer().getPeerBySite("1");
	    	scapCcr.setDestinationHost(route.getHostId());	    	
	    	scapCcr.setDestinationRealm(route.getRealm());	    	
	    	
	    	scapCcr.setOriginHost(ChargingHelper.ORIGIN_HOST);	    	
	    	
	    	scapCcr.addAvp(new CCRequestNumberAvp(0x00)); // DCC::DIRECT_DEBIT
	    	scapCcr.setCCRequestType(dccRequest.getCCRequestType());	    	
	    	scapCcr.addAvp(dccRequest.getAvp(EventTimestampAvp.AVP_CODE));
	    	
	    	if (dccRequest.getRequestedAction() != null)
				scapCcr.setRequestedAction(dccRequest.getRequestedAction());
	    	
	    	logMsg = new StringBuilder("");
	    	logMsg.append("Ccr:SessionId:"+scapCcr.getSessionId());
	    	logMsg.append(":DestinationHost:"+scapCcr.getDestinationHost());
	    	logMsg.append(":DestinationRealm:"+scapCcr.getDestinationRealm());
	    	logMsg.append(":OriginHost:"+scapCcr.getOriginHost());
	    	logMsg.append(":CCRequestType:"+scapCcr.getCCRequestType());
	    	logMsg.append(":RequestedAction:"+scapCcr.getRequestedAction());
	    	
	    	// MSSC
	    	if (dccRequest.getMultipleServicesIndicator() != null) {
	    		//TODO: MSSCC Ind to be confirmed by Per
	    		//scapCcr.addAvp(new MultipleServicesIndicatorAvp(dccRequest.getMultipleServicesIndicator())); 
	    		
	    		// viettel specific (as instructed by Per)
	    		RequestedServiceUnitAvp rsuAvp = new RequestedServiceUnitAvp();
	    		
	    		for (Avp msccAvp: dccRequest.getMultipleServicesCreditControlArray()) {
	    			for (Avp usuAvp: ((DccGrouped)msccAvp).getValues()) {
	    				if (usuAvp.getAvpCode() == UsedServiceUnitAvp.AVP_CODE) {
	    					for (Avp requestedUnits: ((DccGrouped)usuAvp).getValues()) {
	    						// handle cc-total-octets
	    						if (requestedUnits.getAvpCode() == CCTotalOctetsAvp.AVP_CODE) {
	    							CCTotalOctetsAvp cctoAvp = new CCTotalOctetsAvp();
	    							cctoAvp.setData(((CCTotalOctetsAvp)requestedUnits).getValueAsLong());
	    							rsuAvp.addSubAvp(cctoAvp);
	    							//TODO: add RSUcounter=1, for which AVP type is unknown.
	    							
	    							logMsg.append(":CCTotalOctets:"+((CCTotalOctetsAvp)requestedUnits).getValueAsLong());
	    						}
	    						
	    						// handle cc-time
	    						if (requestedUnits.getAvpCode() == CCTimeAvp.AVP_CODE) {
	    							CCTimeAvp cctAvp = new CCTimeAvp();
	    							cctAvp.setData(((CCTimeAvp)requestedUnits).getValue());
	    							rsuAvp.addSubAvp(cctAvp);
	    							
	    							logMsg.append(":CCTime:"+((CCTimeAvp)requestedUnits).getValue());
	    						}
	    						
	    						// handle cc-service-specific-units
	    						if (requestedUnits.getAvpCode() == CCServiceSpecificUnitsAvp.AVP_CODE) {
	    							CCServiceSpecificUnitsAvp ccssuAvp = new CCServiceSpecificUnitsAvp();
	    							ccssuAvp.setData(((CCServiceSpecificUnitsAvp)requestedUnits).getValueAsLong());
	    							rsuAvp.addSubAvp(ccssuAvp);
	    							
	    							logMsg.append(":CCServiceSpecificUnit:"+((CCServiceSpecificUnitsAvp)requestedUnits).getValueAsLong());
	    						}
	    					}
	    				}
	    			}
	    		}
	    		scapCcr.addAvp(rsuAvp);
	    	}
	    	
	    	// Traffic case
	    	int trafficCase = ZteDccHelper.getTrafficCase(dccRequest);
	    	if (trafficCase == 2) {
	    		scapCcr.addAvp(new TrafficCaseAvp(21));
	    		
	    		Avp osiAvp = ZteDccHelper.getOaSubscriptionId(dccRequest);
	    		
	    		OtherPartyIdTypeAvp opitAvp = new OtherPartyIdTypeAvp();
	    		opitAvp.setData(ZteDccHelper.getOaSubscriptionIdType(dccRequest));
	    		
	    		OtherPartyIdDataAvp opidAvp = new OtherPartyIdDataAvp();
	    		opidAvp.setData(ZteDccHelper.getOaSubscriptionIdData(dccRequest));
	    		
	    		OtherPartyIdAvp otherPartyIdAvp = new OtherPartyIdAvp();
	    		otherPartyIdAvp.addSubAvp(opitAvp);
	    		otherPartyIdAvp.addSubAvp(opidAvp);
	    		otherPartyIdAvp.addSubAvp(new OtherPartyIdNatureAvp(0));
	    		
	    		scapCcr.addAvp(otherPartyIdAvp);
	    		
	    		logMsg.append(":SubscriptionIdType:"+opitAvp.getAsUTF8String());
	    		logMsg.append(":SubscriptionIdData:"+opidAvp.getAsUTF8String());
	    	} else {
	    		scapCcr.addAvp(new TrafficCaseAvp(20));
	    		
	    		Avp osiAvp = ZteDccHelper.getOaSubscriptionId(dccRequest);
	    		
	    		OtherPartyIdTypeAvp opitAvp = new OtherPartyIdTypeAvp();
	    		opitAvp.setData(ZteDccHelper.getDaSubscriptionIdType(dccRequest));
	    		
	    		OtherPartyIdDataAvp opidAvp = new OtherPartyIdDataAvp();
	    		opidAvp.setData(ZteDccHelper.getDaSubscriptionIdData(dccRequest));
	    		
	    		OtherPartyIdAvp otherPartyIdAvp = new OtherPartyIdAvp();
	    		otherPartyIdAvp.addSubAvp(opitAvp);
	    		otherPartyIdAvp.addSubAvp(opidAvp);
	    		otherPartyIdAvp.addSubAvp(new OtherPartyIdNatureAvp(0));

	    		scapCcr.addAvp(otherPartyIdAvp);
	    		
	    		logMsg.append(":SubscriptionIdType:"+opitAvp.getAsUTF8String());
	    		logMsg.append(":SubscriptionIdData:"+opidAvp.getAsUTF8String());
	    	}
	    	
	    	// service identifier
	    	scapCcr.addAvp(new ServiceIdentifierAvp(SCAP_SERVICE_IDENTIFIER));
	    	logMsg.append(":ServiceIdentifier:"+SCAP_SERVICE_IDENTIFIER);
	    	// SPI - Service Enabler Type
	    	ServiceParameterInfoAvp spiAvp = ChargingHelper.createSPI(100, Integer.toHexString(ZteDccHelper.getServiceEnablerType(dccRequest)));
	    	scapCcr.addAvp(spiAvp);
	    	
	    	// SPI - Message ID
	    	spiAvp = ChargingHelper.createSPI(500, ZteDccHelper.getMessageId(dccRequest));
	    	scapCcr.addAvp(spiAvp);
	    		    	
	    	// SPI - OA Subscription ID Type
	    	spiAvp = ChargingHelper.createSPI(300, ZteDccHelper.getOaSubscriptionIdType(dccRequest));
	    	scapCcr.addAvp(spiAvp);
	    	
	    	// SPI - OA Subscription ID Data
	    	spiAvp = ChargingHelper.createSPI(301, ZteDccHelper.getOaSubscriptionIdData(dccRequest));
	    	scapCcr.addAvp(spiAvp);
	    	
	    	// SPI - DA Subscription ID Type
	    	spiAvp = ChargingHelper.createSPI(302, ZteDccHelper.getDaSubscriptionIdType(dccRequest));
	    	scapCcr.addAvp(spiAvp);
	    	
	    	// SPI - DA Subscription ID Data
	    	spiAvp = ChargingHelper.createSPI(303, ZteDccHelper.getDaSubscriptionIdData(dccRequest));
	    	scapCcr.addAvp(spiAvp);
	    	
	    	// SPI - SP ID
            spiAvp = ChargingHelper.createSPI(501, ZteDccHelper.getSpId(dccRequest));
            scapCcr.addAvp(spiAvp);
            
            //SPI - ISMP-Info-ChargingType
            spiAvp = ChargingHelper.createSPI(101, ZteDccHelper.getChargingType(dccRequest));
            //TODO: unknown DCC source
            
            // Event-Timestamp
            scapCcr.addAvp(new EventTimestampAvp(new Time(new Date(System.currentTimeMillis()))));
            	
	    	// Timezone
            scapCcr.addAvp(new TimeZoneAvp((byte)11, (byte)0, (byte)0));

			/*
			 * public void format(DataObject dataSource) throws Exception    
			 * {         AddSPI("101",decToHex(dataSource.getValueAsInt(
			 * "Service-Information-ISMP-Information-ChargingType")));     }
			 */		
            
            LogService.stackTraceLog.debug("ChargeAmountProcessor-getScapRequest:Done:"+logMsg.toString());
            logMsg = null;
	    } catch (AvpDataException e) {
	    	//TODO: Log for troubleshooting
	    	LogService.stackTraceLog.debug("ChargeAmountProcessor-getScapRequest:Direct Debit Use Failed!",e);
	    	throw new ServiceLogicException("Direct Debit Use Failed!", e);
	    }
	    LogService.appLog.info("Exiting from DIRECT_DEBIT ChargeAmountProcessor");
		return scapCcr;
    }
}
