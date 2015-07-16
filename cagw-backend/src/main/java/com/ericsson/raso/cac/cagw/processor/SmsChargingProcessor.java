package com.ericsson.raso.cac.cagw.processor;

import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.pps.diameter.dccapi.avp.CCRequestTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCServiceSpecificUnitsAvp;
import com.ericsson.pps.diameter.dccapi.avp.CreditControlAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedActionAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedServiceUnitAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterInfoAvp;
import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdAvp;
import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdDataAvp;
import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdTypeAvp;
import com.ericsson.pps.diameter.dccapi.command.Cca;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.ericsson.pps.diameter.rfcapi.base.avp.AvpDataException;
import com.ericsson.pps.diameter.rfcapi.base.avp.avpdatatypes.Time;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdAvp;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdDataAvp;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdNatureAvp;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdTypeAvp;
import com.ericsson.pps.diameter.scapv2.avp.SubscriptionIdLocationAvp;
import com.ericsson.pps.diameter.scapv2.avp.TimeZoneAvp;
import com.ericsson.pps.diameter.scapv2.avp.TrafficCaseAvp;
import com.ericsson.pps.diameter.scapv2.command.ScapCcr;
import com.ericsson.raso.cac.cagw.SpringHelper;
import com.ericsson.raso.cac.cagw.dao.PersistSmsChargeTransaction;
import com.ericsson.raso.cac.cagw.dao.Transaction;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAccResponse;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinMoMtFlag;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinNotifyMode;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinOperationResult;
import com.satnar.charging.diameter.scap.client.ScapChargingEndpoint;
import com.satnar.common.LogService;
import com.satnar.common.charging.diameter.Peer;
import com.satnar.smpp.CommandStatus;

public class SmsChargingProcessor implements Processor {
    
    private static int SCAP_SERVICE_IDENTIFIER = 4; //as per Mikael's inputs; no mail or written requirements though!!!
    

	@Override
	public void process(Exchange exchange) throws Exception {
	    AuthAcc smppRequest = (AuthAcc) exchange.getIn().getBody();
        LogService.stackTraceLog.info("Request>> " + smppRequest.toString());
        
        try{
			LogService.appLog.debug("Preparing SCAP Request for Auth ACC Request# " + smppRequest.getSmId().getString());
	        Ccr scapRequest = this.getScapRequest(smppRequest);
	        
	        LogService.stackTraceLog.info("Sending SCAP CCR Request to OCC: " + scapRequest.toString());
	        Cca scapResponse = scapRequest.send();
	        LogService.stackTraceLog.info("Received SCAP CCA Response from OCC: " + scapResponse.toString());
	        
	        LogService.appLog.debug("Preparing Auth ACC Response for Request# " + smppRequest.getSmId().getString());
	        AuthAccResponse smppResponse = this.getSmppResponse(scapResponse, smppRequest);
	        smppResponse.setCommandSequence(smppRequest.getCommandSequence());
	        
	        LogService.appLog.debug("Spawning persistence thread for Request# " + smppRequest.getSmId().getString());
	        Transaction smsChargingStatus = this.getSmsChargingStatus(smppRequest, scapRequest, smppResponse);
	        LogService.appLog.debug("Verify Transaction pojo(" + smsChargingStatus + ") for Request# " + smppRequest.getSmId().getString());
	        new Thread(new PersistSmsChargeTransaction(smsChargingStatus)).start();
	        
	        LogService.stackTraceLog.info("Response>> " + smppResponse.toString());
	        exchange.getOut().setBody(smppResponse);
		}catch (Exception genE){//Added for troubleshooting
			LogService.appLog.debug("SmsChargingProcessor-process:Encountered exception.",genE); 
			AuthAccResponse smppResponse = this.getUnknownFailureSmppResponse(smppRequest);
            LogService.stackTraceLog.info("Response>> " + smppResponse.toString());
            exchange.getOut().setBody(smppResponse);
		}
	}
	
	private AuthAccResponse getUnknownFailureSmppResponse(AuthAcc smppRequest) {
	    AuthAccResponse smppResponse = new AuthAccResponse();
	    smppResponse.setCommandStatus(CommandStatus.ESME_ROK);
        smppResponse.setOperationResult(WinOperationResult.OTHER_ERRORS);
        smppResponse.setNotifyMode(WinNotifyMode.NOTIFY_FAILURE);   
        smppResponse.setCommandSequence(smppRequest.getCommandSequence());
        smppResponse.getCommandLength();
        LogService.appLog.debug("Constructed Failure AuthAccResponse: " + smppResponse.toString());
        return smppResponse;
    }

    private Transaction getSmsChargingStatus(AuthAcc smppRequest, Ccr scapRequest, AuthAccResponse smppResponse) {
        Transaction status = new Transaction();
        LogService.appLog.debug("Preparing persistence pojo for transaction state - Req# " + smppRequest.getSmId().getString());
        
        status.setSourceAddress(smppRequest.getSourceAddress().getString());
        status.setDestinationAddress(smppRequest.getDestinationAddress().getString());
        status.setMessageId(smppRequest.getSmId().getString());
        status.setTransactionId(smppRequest.getCommandSequence().getValue());
        status.setTransactionTime(System.currentTimeMillis());
        
        if (smppRequest.getMoMtFlag() == WinMoMtFlag.MO)
            status.setChargedParty(smppRequest.getSourceAddress().getString());
        else
            status.setChargedParty(smppRequest.getDestinationAddress().getString());
        
        status.setTransactionId(smppRequest.getCommandSequence().getValue());
        
        try {
            status.setChargingSessionId(scapRequest.getSessionId());
        } catch (AvpDataException e) {
            status.setChargingSessionId(ChargingHelper.createChargingSessionId(smppRequest));
        }
        
        if (smppResponse.getOperationResult() == WinOperationResult.SUCCESS)
            status.setChargeStatus(true);
        else
            status.setChargeStatus(false);
        LogService.appLog.debug("Verify Transaction state pojo: " + status);
        
        return status;
    }

    private AuthAccResponse getSmppResponse(Cca scapResponse, AuthAcc smppRequest) throws ServiceLogicException {
        AuthAccResponse smppResponse = new AuthAccResponse();
        
        try {
            smppResponse.setCommandStatus(CommandStatus.ESME_ROK);
            long scapResult = scapResponse.getResultCode();
            smppResponse.setOperationResult(ChargingHelper.getWinOperationResult(scapResult, smppRequest.getMoMtFlag()));
            smppResponse.setNotifyMode(WinNotifyMode.NOTIFY_FAILURE);  
            smppResponse.setCommandSequence(smppRequest.getCommandSequence());
            smppResponse.getCommandLength();
            LogService.appLog.debug("Constructed Functional AuthAccResponse: " + smppResponse.toString());

            return smppResponse;
        } catch (AvpDataException e) {
        	LogService.appLog.debug("SmsChargingProcessor-getSmppResponse:SmppCommandSequence:"+smppRequest.getCommandSequence().getValue()+
        			"Backed or Response Processing Failure!!",e);
            throw new ServiceLogicException("Unable to unpack SCAP Response!!", e);
        }
        
    }

    private Ccr getScapRequest(AuthAcc smppRequest) throws ServiceLogicException {
        Ccr dccCcr = null;
        StringBuilder logMsg = null;
	    try {
	        ScapChargingEndpoint scapEndpoint = (ScapChargingEndpoint) SpringHelper.getScapDiameter();
	        LogService.appLog.debug("SCAP Endpoint as configured in CAMEL available: " + (scapEndpoint != null));
	        
	        if(scapEndpoint == null) {
	            LogService.appLog.error("SCAP Endpoint instance could not be acquired!!");
	            throw new ServiceLogicException("Backend (SCAP Endpoint) not available for processing this request# " + smppRequest.getSmId().getString());
	        }
	        
	        dccCcr = new Ccr(ChargingHelper.createChargingSessionId(smppRequest), 
	                scapEndpoint.getDccStack().getDiameterStack(), 
	                ChargingHelper.SERVICE_CONTEXT_ID);
	        LogService.appLog.debug("DCC CCR (SCAP Variant) created for request# " + smppRequest.getSmId().getString()); 
	        
	        logMsg = new StringBuilder("");
	        logMsg.append("Ccr::SessionId:"+dccCcr.getSessionId());
            
	        // things that we can manage on our own...
	        Peer route = scapEndpoint.getScapLoadBalancer().getPeerBySite("1"); 
	        dccCcr.setDestinationHost(route.getHostId()); logMsg.append(", DestinationHost:"+dccCcr.getDestinationHost());
	        dccCcr.setDestinationRealm(route.getRealm()); logMsg.append(", DestinationRealm:"+dccCcr.getDestinationRealm());
	        dccCcr.setOriginHost(scapEndpoint.getOriginRealm()); logMsg.append(", OriginHost:"+dccCcr.getOriginHost());
	        dccCcr.setOriginRealm(scapEndpoint.getOriginRealm()); logMsg.append(", OriginRealm:"+dccCcr.getOriginRealm());
	        dccCcr.setCCRequestNumber(0x00);  logMsg.append(", CCRequestNumber:"+dccCcr.getCCRequestNumber()); // DCC::DIRECT_DEBIT
	        dccCcr.setCCRequestType(CCRequestTypeAvp.EVENT_REQUEST); logMsg.append(", CCRequestType:"+dccCcr.getCCRequestType());
	        dccCcr.setRequestedAction(RequestedActionAvp.DIRECT_DEBITING); logMsg.append(", RequestedAction:"+dccCcr.getRequestedAction());
	        LogService.appLog.debug("Checkpoint#1 - SCAP CCR Build: " + logMsg.toString());
	        
	        
	        // things we expect shit from Viettel...
	        RequestedServiceUnitAvp rsuAvp = new RequestedServiceUnitAvp();
	        CCServiceSpecificUnitsAvp ssuAvp = new CCServiceSpecificUnitsAvp();
	        ssuAvp.setData(1); // 1 SMS unit to charge
	        rsuAvp.addSubAvp(ssuAvp);
	        dccCcr.setRequestedServiceUnit(rsuAvp);
            logMsg.append(", RequestedServicesUnit->ServiceSpecificUnits(1)");
	        LogService.appLog.debug("Checkpoint#2 - RSU added to CCR: " + logMsg.toString());
            
	    	if (smppRequest.getMoMtFlag() == WinMoMtFlag.MO) {
	            dccCcr.addAvp(new TrafficCaseAvp(21)); //MO Charging
	            logMsg.append(", TrafficCase(21)");
	            LogService.appLog.debug("Checkpoint#2 - TrafficCase added to CCR: " + logMsg.toString());
	                
	            
	            OtherPartyIdAvp opiAvp = new OtherPartyIdAvp();
	            OtherPartyIdTypeAvp opitAvp = new OtherPartyIdTypeAvp(OtherPartyIdTypeAvp.END_USER_E164);
	            OtherPartyIdDataAvp opidAvp = new OtherPartyIdDataAvp(smppRequest.getSourceAddress().getString());
	            OtherPartyIdNatureAvp opinAvp = new OtherPartyIdNatureAvp(OtherPartyIdNatureAvp.UNKNOWN);
	            opiAvp.addSubAvp(opitAvp);
	            opiAvp.addSubAvp(opidAvp);
	            opiAvp.addSubAvp(opinAvp);
	            dccCcr.addAvp(opiAvp);
                
	            
	            SubscriptionIdAvp siAvp = new SubscriptionIdAvp();
                SubscriptionIdTypeAvp sitAvp = new SubscriptionIdTypeAvp(SubscriptionIdTypeAvp.END_USER_E164);
                SubscriptionIdDataAvp sidAvp = new SubscriptionIdDataAvp(smppRequest.getSourceAddress().getString());
                siAvp.addSubAvp(sitAvp);
                siAvp.addSubAvp(sidAvp);
                dccCcr.addAvp(siAvp);
                
	            
	            logMsg.append(", OtherPartyId[OtherPartyType("+ OtherPartyIdTypeAvp.END_USER_E164 + ")), OtherPartyIdNature(" + OtherPartyIdNatureAvp.UNKNOWN 
	                    + "), OtherPartyIdData(" + smppRequest.getSourceAddress().getString() + ")");
	            LogService.appLog.debug("Checkpoint#3 - OPI added to CCR: " + logMsg.toString());
	            
	            
	        } else {
	            dccCcr.addAvp(new TrafficCaseAvp(20)); //MT Charging
	            logMsg.append(", TrafficCase(20)");
                LogService.appLog.debug("Checkpoint#2 - TrafficCase added to CCR: " + logMsg.toString());

	            OtherPartyIdAvp opiAvp = new OtherPartyIdAvp();
	            OtherPartyIdTypeAvp opitAvp = new OtherPartyIdTypeAvp(OtherPartyIdTypeAvp.END_USER_E164);
	            OtherPartyIdDataAvp opidAvp = new OtherPartyIdDataAvp(smppRequest.getDestinationAddress().getString());
	            OtherPartyIdNatureAvp opinAvp = new OtherPartyIdNatureAvp(OtherPartyIdNatureAvp.UNKNOWN);
	            opiAvp.addSubAvp(opitAvp);
	            opiAvp.addSubAvp(opidAvp);
	            opiAvp.addSubAvp(opinAvp);
	            dccCcr.addAvp(opiAvp);
	            
	            SubscriptionIdAvp siAvp = new SubscriptionIdAvp();
	            SubscriptionIdTypeAvp sitAvp = new SubscriptionIdTypeAvp(SubscriptionIdTypeAvp.END_USER_E164);
	            SubscriptionIdDataAvp sidAvp = new SubscriptionIdDataAvp(smppRequest.getDestinationAddress().getString());
	            siAvp.addSubAvp(sitAvp);
	            siAvp.addSubAvp(sidAvp);
	            dccCcr.addAvp(siAvp);

	            
	            logMsg.append(", OtherPartyId[OtherPartyType("+ OtherPartyIdTypeAvp.END_USER_E164 + ")), OtherPartyIdNature(" + OtherPartyIdNatureAvp.UNKNOWN 
                        + "), OtherPartyIdData(" + smppRequest.getSourceAddress().getString() + ")");
                LogService.appLog.debug("Checkpoint#3 - OPI added to CCR: " + logMsg.toString());
                
	        }
	        
	    	
	        // service identifier
	    	dccCcr.setServiceIdentifier(SCAP_SERVICE_IDENTIFIER);
	        logMsg.append(", ServiceIdentifier(" + SCAP_SERVICE_IDENTIFIER + ")");
            LogService.appLog.debug("Checkpoint#4 - ServiceIdentifier added to CCR: " + logMsg.toString());
            
	        // Roaming indicator (subscribe-id-location)
            dccCcr.addAvp(new SubscriptionIdLocationAvp(smppRequest.getMoMscAddress().getString()));
	        logMsg.append(", SubscriptionIdLocation(" + smppRequest.getMoMscAddress().getString() + ")");
            LogService.appLog.debug("Checkpoint#5 - SubscriptionIdLocation added to CCR: " + logMsg.toString());
            
	        // service paramter info set
            ServiceParameterInfoAvp spiAvp =  ChargingHelper.createSPI(100, SCAP_SERVICE_IDENTIFIER); // service enabler type
            dccCcr.addAvp(spiAvp);
	        logMsg.append(", SPI(100=" + SCAP_SERVICE_IDENTIFIER + ")");
            LogService.appLog.debug("Checkpoint#6 - SPI(100=ServiceIdentifier) added to CCR: " + logMsg.toString());
            
	        spiAvp = ChargingHelper.createSPI(500, smppRequest.getSmId().getString()); // message id
	        dccCcr.addAvp(spiAvp);
	        logMsg.append(", SPI(500=" + smppRequest.getSmId().getString() + ")");
            LogService.appLog.debug("Checkpoint#7 - SPI(500=SMS Message ID) added to CCR: " + logMsg.toString());
            
	        spiAvp = ChargingHelper.createSPI(300, OtherPartyIdTypeAvp.END_USER_E164); // source addr type
	        dccCcr.addAvp(spiAvp);
	        logMsg.append(", SPI(300=" + OtherPartyIdTypeAvp.END_USER_E164 + ")");
            LogService.appLog.debug("Checkpoint#8 - SPI(300=A Party Type) added to CCR: " + logMsg.toString());
            
	        spiAvp = ChargingHelper.createSPI(301, smppRequest.getSourceAddress().getString()); // source addr
	        dccCcr.addAvp(spiAvp);
	        logMsg.append(", SPI(301=" + smppRequest.getSourceAddress().getString() + ")");
            LogService.appLog.debug("Checkpoint#9 - SPI(301=A Party Number) added to CCR: " + logMsg.toString());
            
	        spiAvp = ChargingHelper.createSPI(302, OtherPartyIdTypeAvp.END_USER_E164); // destn addr type
	        dccCcr.addAvp(spiAvp);
	        logMsg.append(", SPI(302=" + OtherPartyIdTypeAvp.END_USER_E164 + ")");
            LogService.appLog.debug("Checkpoint#10 - SPI(302=B Party Type) added to CCR: " + logMsg.toString());
            
	        spiAvp = ChargingHelper.createSPI(303, smppRequest.getDestinationAddress().getString()); // destn addr
	        dccCcr.addAvp(spiAvp);
	        logMsg.append(", SPI(303=" + smppRequest.getDestinationAddress().getString() + ")");
            LogService.appLog.debug("Checkpoint#11 - SPI(303=B Party Number) added to CCR: " + logMsg.toString());
            
	        spiAvp = ChargingHelper.createSPI(304, smppRequest.getSmscAddress().getString()); // smsc addr
	        dccCcr.addAvp(spiAvp);
            logMsg.append(", SPI(304=" + smppRequest.getSmscAddress().getString() + ")");
            LogService.appLog.debug("Checkpoint#12 - SPI(301=SMSC Number) added to CCR: " + logMsg.toString());
            
            spiAvp = ChargingHelper.createSPI(305, smppRequest.getMoMscAddress().getString()); // mo msc addr
            dccCcr.addAvp(spiAvp);
            logMsg.append(", SPI(305=" + smppRequest.getSmscAddress().getString() + ")");
            LogService.appLog.debug("Checkpoint#13 - SPI(305=MSC Number) added to CCR: " + logMsg.toString());
            
            // Event-Timestamp
            Date timestamp = new Date(System.currentTimeMillis());
            dccCcr.setEventTimestamp(new Time(timestamp));
            logMsg.append(", EventTimestamp(" + timestamp + ")");
            LogService.appLog.debug("Checkpoint#14 - EventTimestamp added to CCR: " + logMsg.toString());
            
	        // Timezone
            dccCcr.addAvp(new TimeZoneAvp((byte)11, (byte)0, (byte)0));
	        logMsg.append(", TimeZone(11hours 0mins 0dst)");
            LogService.appLog.debug("Checkpoint#15 - TimeZone added to CCR: " + logMsg.toString());
            
            // sanity
            dccCcr.setAuthApplicationId(0x04);
            dccCcr.setCCCorrelationId(dccCcr.getSessionId().getBytes());
            
	        LogService.stackTraceLog.debug("SmsChargingProcessor-getScapRequest: Final Construction:" + logMsg.toString());
            logMsg = null;
            
	        return dccCcr;
	    } catch (Exception e) {
	    	LogService.stackTraceLog.debug("SmsChargingProcessor-getScapRequest:"+smppRequest.getCommandSequence().getValue()+
	    			":SmId:"+smppRequest.getSmId().getString()+
        			"Failed processing the request!!",e);
	        throw new ServiceLogicException("Request Parameters failed creating Backend(SCAP) Charging Request!!", e);
	    }
	}
}
