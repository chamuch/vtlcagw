package com.ericsson.raso.cac.cagw.processor;

import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterInfoAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterValueAvp;
import com.ericsson.raso.cac.cagw.SpringHelper;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinMoMtFlag;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinOperationResult;
import com.satnar.charging.diameter.scap.client.ScapChargingEndpoint;
import com.satnar.common.LogService;
import com.satnar.common.charging.diameter.ResultCode;

public class ChargingHelper {
    
    public static final String SERVICE_CONTEXT_ID = "SCAP_V.2.0@ericsson.com";
    public static final String ORIGIN_HOST        = "cagw.cac.raso.ericsson.com";
    
    public static String createChargingSessionId(AuthAcc smppRequest) {
        String ownRealm = ((ScapChargingEndpoint)SpringHelper.getScapDiameter()).getOriginRealm();
        int transactionId = smppRequest.getCommandSequence().getValue();
        String msgId = smppRequest.getSmId().getString();
        
        return msgId + "-" + Integer.toHexString(transactionId) + "@" + ownRealm;
        
    }
    
    public static ServiceParameterInfoAvp createSPI(int label, String value) {
        ServiceParameterInfoAvp spiAvp = new ServiceParameterInfoAvp();
        spiAvp.addSubAvp(new ServiceParameterTypeAvp(label));
        ServiceParameterValueAvp spiValueAvp = new ServiceParameterValueAvp();
        spiValueAvp.setData(value);
        spiAvp.addSubAvp(spiValueAvp);
        return spiAvp;
    }
    
    public static ServiceParameterInfoAvp createSPI(int label, int value) {
        ServiceParameterInfoAvp spiAvp = new ServiceParameterInfoAvp();
        spiAvp.addSubAvp(new ServiceParameterTypeAvp(label));
        ServiceParameterValueAvp spiValueAvp = new ServiceParameterValueAvp();
        spiValueAvp.setData(value);
        spiAvp.addSubAvp(spiValueAvp);
        return spiAvp;
    }

    public static WinOperationResult getWinOperationResult(long scapResult, WinMoMtFlag moMtFlag) {
        if (scapResult == ResultCode.DIAMETER_SUCCESS.getCode() || scapResult == ResultCode.DIAMETER_CREDIT_CONTROL_NOT_APPLICABLE.getCode()) { 
            LogService.appLog.info("Received DIAMETER_SUCCESS or DIAMETER_CREDIT_CONTROL_NOT_APPLICABLE. Assuming success!!");
            return WinOperationResult.SUCCESS;
        }
        
        if (scapResult == ResultCode.SUBSCRIBER_NOT_FOUND.getCode()) {
            if (moMtFlag == WinMoMtFlag.MO) {
                LogService.appLog.info("Received SUBSCRIBER_NOT_FOUND. Mapping to MO_USER_ACCT_NOT_EXIST!!");
                return WinOperationResult.MO_USER_ACCT_NOT_EXIST;
            } else {
                LogService.appLog.info("Received SUBSCRIBER_NOT_FOUND. Mapping to MT_USER_ACCT_NOT_EXIST!!");
                return WinOperationResult.MT_USER_ACCT_NOT_EXIST;
            }
        }
        
        if (scapResult == ResultCode.SUBSCRIBER_GRACE_NOT_ALLOWED.getCode() || 
                scapResult == ResultCode.SUBSCRIBER_LOCKED.getCode() || 
                scapResult == ResultCode.SUBSCRIBER_PREACTIVE_NOT_ALLOWED.getCode() || 
                scapResult == ResultCode.SUBSCRIBER_RECYCLE_NOT_ALLOWED.getCode()) {
            
            if (moMtFlag == WinMoMtFlag.MO) {
                LogService.appLog.info("Received SUBSCRIBER_GRACE_NOT_ALLOWED/ SUBSCRIBER_LOCKED/ SUBSCRIBER_PREACTIVE_NOT_ALLOWED/ SUBSCRIBER_RECYCLE_NOT_ALLOWED. "
                        + "Mapping to MO_USER_ACCT_NOT_CORRECT!!");
                return WinOperationResult.MO_USER_ACCT_NOT_CORRECT;
            } else {
                LogService.appLog.info("Received SUBSCRIBER_GRACE_NOT_ALLOWED/ SUBSCRIBER_LOCKED/ SUBSCRIBER_PREACTIVE_NOT_ALLOWED/ SUBSCRIBER_RECYCLE_NOT_ALLOWED. "
                        + "Mapping to MT_USER_ACCT_NOT_CORRECT!!");
                return WinOperationResult.MT_USER_ACCT_NOT_CORRECT;
            }
        }
        
        if (scapResult == ResultCode.SUBSCRIBER_INSUFFICIENT_BALANCE.getCode() || scapResult == ResultCode.DIAMETER_CREDIT_LIMIT_REACHED.getCode()) {
            if (moMtFlag == WinMoMtFlag.MO) {
                LogService.appLog.info("Received SUBSCRIBER_INSUFFICIENT_BALANCE/ DIAMETER_CREDIT_LIMIT_REACHED. Mapping to MO_USER_ACCT_NOT_ENOUGH_CREDIT!!");
                return WinOperationResult.MO_USER_ACCT_NOT_ENOUGH_CREDIT;
            } else {
                LogService.appLog.info("Received SUBSCRIBER_INSUFFICIENT_BALANCE/ DIAMETER_CREDIT_LIMIT_REACHED. Mapping to MT_USER_ACCT_NOT_ENOUGH_CREDIT!!");
                return WinOperationResult.MT_USER_ACCT_NOT_ENOUGH_CREDIT;
            }
        }

        LogService.appLog.info("Received unknown resultCode(" + scapResult + "). Mapping to OTHER_ERRORS!!");
        return WinOperationResult.OTHER_ERRORS;
    }
    
}
