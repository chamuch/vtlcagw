package com.ericsson.raso.cac.cagw.processor;

import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.pps.diameter.dccapi.avp.CCRequestNumberAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCRequestTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCServiceSpecificUnitsAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedActionAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedServiceUnitAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceIdentifierAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterInfoAvp;
import com.ericsson.pps.diameter.dccapi.command.Cca;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.ericsson.pps.diameter.rfcapi.base.avp.AvpDataException;
import com.ericsson.pps.diameter.rfcapi.base.avp.EventTimestampAvp;
import com.ericsson.pps.diameter.rfcapi.base.avp.avpdatatypes.Time;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdAvp;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdDataAvp;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdNatureAvp;
import com.ericsson.pps.diameter.scapv2.avp.OtherPartyIdTypeAvp;
import com.ericsson.pps.diameter.scapv2.avp.SubscriptionIdLocationAvp;
import com.ericsson.pps.diameter.scapv2.avp.TimeZoneAvp;
import com.ericsson.pps.diameter.scapv2.avp.TrafficCaseAvp;
import com.ericsson.raso.cac.cagw.SpringHelper;
import com.ericsson.raso.cac.cagw.dao.PersistSmsChargeTransaction;
import com.ericsson.raso.cac.cagw.dao.Transaction;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAccResponse;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinMoMtFlag;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinNotifyMode;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinOperationResult;
import com.satnar.charging.diameter.Peer;
import com.satnar.charging.diameter.scap.client.ScapChargingEndpoint;
import com.satnar.common.LogService;
import com.satnar.smpp.CommandStatus;

public class SmsChargingProcessor implements Processor {
    
    private static int SCAP_SERVICE_IDENTIFIER = 4; //as per Mikael's inputs; no mail or written requirements though!!!
    

	@Override
	public void process(Exchange exchange) throws Exception {
		//System.out.println("We have entered into SmsChargingProcessor");
		
		AuthAcc smppRequest = (AuthAcc) exchange.getIn().getBody();
        Ccr scapRequest = this.getScapRequest(smppRequest);
        
        StringBuilder logMsg = new StringBuilder("");
	    logMsg.append(":CommandSequence:");logMsg.append(smppRequest.getCommandSequence().getValue());
	    logMsg.append(":SmId:");logMsg.append(smppRequest.getCommandSequence().getValue());
	    logMsg.append(":SourceAddres:");logMsg.append(smppRequest.getCommandSequence().getValue());
	    logMsg.append(":DestinationAddress:");logMsg.append(smppRequest.getCommandSequence().getValue());
	    LogService.appLog.debug("SmsChargingProcessor-process:Sending.."+logMsg.toString());
	    
        Cca scapResponse = scapRequest.send();
        AuthAccResponse smppResponse = this.getSmppResponse(scapResponse, smppRequest);
        smppResponse.setCommandSequence(smppRequest.getCommandSequence());
        
        Transaction smsChargingStatus = this.getSmsChargingStatus(smppRequest, scapRequest, smppResponse);
        new Thread(new PersistSmsChargeTransaction(smsChargingStatus)).start();
        
        exchange.getOut().setBody(smppResponse);
                
	    logMsg.append(":ScapResultCode:");logMsg.append(scapResponse.getResultCode());	    
        LogService.appLog.debug("SmsChargingProcessor-process:Done."+logMsg.toString());
        logMsg = null;
	}
	
	private Transaction getSmsChargingStatus(AuthAcc smppRequest, Ccr scapRequest, AuthAccResponse smppResponse) {
        Transaction status = new Transaction();
        
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
        
        LogService.stackTraceLog.debug("SmsChargingProcessor-getSmsChargingStatus:Done.SmppCommandSequence:"+smppRequest.getCommandSequence().getValue());
        
        return status;
    }

    private AuthAccResponse getSmppResponse(Cca scapResponse, AuthAcc smppRequest) throws ServiceLogicException {
        AuthAccResponse smppResponse = new AuthAccResponse();
        
        try {
            smppResponse.setCommandStatus(CommandStatus.ESME_ROK);
            long scapResult = scapResponse.getResultCode();
            smppResponse.setOperationResult(ChargingHelper.getWinOperationResult(scapResult, smppRequest.getMoMtFlag()));
            smppResponse.setNotifyMode(WinNotifyMode.NOTIFY_FAILURE);    
            
            LogService.stackTraceLog.debug("SmsChargingProcessor-getSmppResponse:Done.SmppCommandSequence:"+smppRequest.getCommandSequence().getValue());
        } catch (AvpDataException e) {
        	LogService.stackTraceLog.debug("SmsChargingProcessor-getSmppResponse:SmppCommandSequence:"+smppRequest.getCommandSequence().getValue()+
        			"Backed or Response Processing Failure!!",e);
            throw new ServiceLogicException("Backed or Response Processing Failure!!", e);
        }
        
        return smppResponse;
    }

    private Ccr getScapRequest(AuthAcc smppRequest) throws ServiceLogicException {
	    Ccr scapCcr = null;
	    StringBuilder logMsg = null;
	    try {
	        ScapChargingEndpoint scapEndpoint = (ScapChargingEndpoint) SpringHelper.getScapDiameter();
	        scapCcr = new Ccr(ChargingHelper.createChargingSessionId(smppRequest), 
	                scapEndpoint.getDccStack().getDiameterStack(), 
	                ChargingHelper.SERVICE_CONTEXT_ID);
	        
	        Peer route = scapEndpoint.getScapLoadBalancer().getRoute();
	        scapCcr.setDestinationHost(route.getHostId());
	        scapCcr.setDestinationRealm(route.getRealm());
	        scapCcr.setOriginHost(ChargingHelper.ORIGIN_HOST);
	        scapCcr.addAvp(new CCRequestNumberAvp(0x00)); // DCC::DIRECT_DEBIT
	        scapCcr.setCCRequestType(CCRequestTypeAvp.EVENT_REQUEST);
	        scapCcr.setRequestedAction(RequestedActionAvp.DIRECT_DEBITING);
	        
	        RequestedServiceUnitAvp rsuAvp = new RequestedServiceUnitAvp();
	        CCServiceSpecificUnitsAvp ssuAvp = new CCServiceSpecificUnitsAvp();
	        ssuAvp.setData(1); // 1 SMS unit to charge
	        rsuAvp.addSubAvp(ssuAvp);
	        scapCcr.addAvp(rsuAvp);
	        
	        logMsg = new StringBuilder("");
	        logMsg.append("Smpp:CommandSequence:"+smppRequest.getCommandSequence().getValue());
	    	logMsg.append("Ccr:SessionId:"+scapCcr.getSessionId());
	    	logMsg.append(":DestinationHost:"+scapCcr.getDestinationHost());
	    	logMsg.append(":DestinationRealm:"+scapCcr.getDestinationRealm());
	    	logMsg.append(":OriginHost:"+scapCcr.getOriginHost());
	    	logMsg.append(":CCRequestType:"+scapCcr.getCCRequestType());
	    	logMsg.append(":RequestedAction:"+scapCcr.getRequestedAction());
	    	logMsg.append(":CCServiceSpecificUnit:1");
	    	
	        if (smppRequest.getMoMtFlag() == WinMoMtFlag.MO) {
	            scapCcr.addAvp(new TrafficCaseAvp(21)); //MO Charging
	            
	            OtherPartyIdAvp opiAvp = new OtherPartyIdAvp();
	            OtherPartyIdTypeAvp opitAvp = new OtherPartyIdTypeAvp(OtherPartyIdTypeAvp.END_USER_E164);
	            OtherPartyIdDataAvp opidAvp = new OtherPartyIdDataAvp(smppRequest.getSourceAddress().getString());
	            OtherPartyIdNatureAvp opinAvp = new OtherPartyIdNatureAvp(OtherPartyIdNatureAvp.UNKNOWN);
	            opiAvp.addSubAvp(opitAvp);
	            opiAvp.addSubAvp(opidAvp);
	            opiAvp.addSubAvp(opinAvp);
	            scapCcr.addAvp(opiAvp);
	            
	            logMsg.append(":SubscriptionIdType:"+opitAvp.getAsUTF8String());
	    		logMsg.append(":SubscriptionIdData:"+opidAvp.getAsUTF8String());
	        } else {
	            scapCcr.addAvp(new TrafficCaseAvp(20)); //MT Charging
	            
	            OtherPartyIdAvp opiAvp = new OtherPartyIdAvp();
	            OtherPartyIdTypeAvp opitAvp = new OtherPartyIdTypeAvp(OtherPartyIdTypeAvp.END_USER_E164);
	            OtherPartyIdDataAvp opidAvp = new OtherPartyIdDataAvp(smppRequest.getDestinationAddress().getString());
	            OtherPartyIdNatureAvp opinAvp = new OtherPartyIdNatureAvp(OtherPartyIdNatureAvp.UNKNOWN);
	            opiAvp.addSubAvp(opitAvp);
	            opiAvp.addSubAvp(opidAvp);
	            opiAvp.addSubAvp(opinAvp);
	            scapCcr.addAvp(opiAvp);
	            
	            logMsg.append(":SubscriptionIdType:"+opitAvp.getAsUTF8String());
	    		logMsg.append(":SubscriptionIdData:"+opidAvp.getAsUTF8String());
	        }
	        
	        // service identifier
	        scapCcr.addAvp(new ServiceIdentifierAvp(SCAP_SERVICE_IDENTIFIER));
	        
	        // Roaming indicator (subscribe-id-location)
	        scapCcr.addAvp(new SubscriptionIdLocationAvp(smppRequest.getMoMscAddress().getString()));
	        
	        // service paramter info set
	        ServiceParameterInfoAvp spiAvp =  ChargingHelper.createSPI(100, SCAP_SERVICE_IDENTIFIER); // service enabler type
	        scapCcr.addAvp(spiAvp);
	        
	        spiAvp = ChargingHelper.createSPI(500, smppRequest.getSmId().getString()); // message id
	        scapCcr.addAvp(spiAvp);
	        
	        spiAvp = ChargingHelper.createSPI(300, OtherPartyIdTypeAvp.END_USER_E164); // source addr type
	        scapCcr.addAvp(spiAvp);
	        
	        spiAvp = ChargingHelper.createSPI(301, smppRequest.getSourceAddress().getString()); // source addr
	        scapCcr.addAvp(spiAvp);
	        
	        spiAvp = ChargingHelper.createSPI(302, OtherPartyIdTypeAvp.END_USER_E164); // destn addr type
	        scapCcr.addAvp(spiAvp);
	        
	        spiAvp = ChargingHelper.createSPI(303, smppRequest.getDestinationAddress().getString()); // destn addr
	        scapCcr.addAvp(spiAvp);
	        
	        spiAvp = ChargingHelper.createSPI(304, smppRequest.getSmscAddress().getString()); // smsc addr
            scapCcr.addAvp(spiAvp);
            
            spiAvp = ChargingHelper.createSPI(305, smppRequest.getMoMscAddress().getString()); // mo msc addr
            scapCcr.addAvp(spiAvp);
            
            logMsg.append(":MessageId:"+smppRequest.getSmId().getString());
            logMsg.append(":SourceAddress:"+smppRequest.getSourceAddress().getString());
            logMsg.append(":DestinationAddress:"+smppRequest.getDestinationAddress().getString());
            logMsg.append(":SmscAddress:"+smppRequest.getSmscAddress().getString());
            logMsg.append(":MoMscAddress:"+smppRequest.getMoMscAddress().getString());
	        
	        // Event-Timestamp
	        scapCcr.addAvp(new EventTimestampAvp(new Time(new Date(System.currentTimeMillis()))));
	        
	        // Timezone
	        scapCcr.addAvp(new TimeZoneAvp((byte)11, (byte)0, (byte)0));
	        
	        LogService.stackTraceLog.debug("SmsChargingProcessor-getSmppResponse:Done:"+logMsg.toString());
            logMsg = null;
            
	        return scapCcr;
	    } catch (Exception e) {
	    	LogService.stackTraceLog.debug("SmsChargingProcessor-getSmppResponse:CommandSequence:"+smppRequest.getCommandSequence().getValue()+
	    			":SmId:"+smppRequest.getSmId().getString()+
        			"Failed processing the request!!",e);
	        throw new ServiceLogicException("Failed processing the request!!", e);
	    }
	}
}
