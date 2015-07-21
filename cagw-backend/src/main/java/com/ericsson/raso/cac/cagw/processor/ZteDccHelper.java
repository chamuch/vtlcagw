package com.ericsson.raso.cac.cagw.processor;

import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdDataAvp;
import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.avpdatatypes.DccGrouped;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.rfcapi.base.avp.AvpDataException;
import com.satnar.common.LogService;

public class ZteDccHelper {
	
    public static final int ZTE_SERVICE_INFORMATION  = 873;
    public static final int ZTE_ISMP_INFORMATION     = 20500;
    public static final int ZTE_MESSAGE_ID           = 20501;
    public static final int ZTE_CHARGE_PARTY_TYPE    = 20502;
    public static final int ZTE_SP_ID                = 20504;
    public static final int ZTE_SERVICE_ENABLER_TYPE = 20505;
    public static final int ZTE_OA_SUBSCRIPTION_ID   = 20511;
    public static final int ZTE_DA_SUBSCRIPTION_ID   = 20512;
    public static final int ZTE_CHARGING_TYPE        = 20515;
	
	public static int getTrafficCase(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");

		    // find ZTE ISMP Group
		    Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		    if (ismpAvp == null) {
		        LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		        throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		    }
		    
		    Avp chargePartyTypeAvp = getSubAvp(ismpAvp, ZTE_CHARGE_PARTY_TYPE);
		    if (chargePartyTypeAvp == null) {
		        LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_CHARGE_PARTY_TYPE (" + ZTE_CHARGE_PARTY_TYPE + ")");
                throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_CHARGE_PARTY_TYPE (" + ZTE_CHARGE_PARTY_TYPE + ")");
		    }
		    
		    try {
                return chargePartyTypeAvp.getAsInt();
            } catch (AvpDataException e) {
                LogService.appLog.error(String.format("Bad Avp Data!! Unable to read ChargedPartyTypeAvp: %s", chargePartyTypeAvp));
                throw new ServiceLogicException(String.format("Bad Avp Data!! Unable to read ChargedPartyTypeAvp: %s", chargePartyTypeAvp));
            }
	}
	
	public static Avp getSubAvp(Avp parent, int avpCode) throws ServiceLogicException {
	    Avp result = null;
	    try {
        for (Avp avp: parent.getDataAsGroup()) 
            if (avp.getAvpCode() == avpCode)
                result = avp;
	    } catch (AvpDataException e) {
	        LogService.appLog.error(String.format("Bad Avp Data!! Unable to find child avp with code: %s within avp: %s", avpCode, parent));
            throw new ServiceLogicException(String.format("Bad Avp Data!! Unable to find child avp with code: %s within avp: %s", avpCode, parent));
	    }
	    return result;
	}
	
	public static Avp getServiceInformation(MmsDccCharge dccRequest) {
		return dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
	}
	
	public static Avp getServiceInfoIsmp(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		}
		return getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
	}
	
	public static Avp getOaSubscriptionId(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		}
		
		Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		if (ismpAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		}
		
		Avp osiAvp = getSubAvp(ismpAvp, ZTE_OA_SUBSCRIPTION_ID);
		if (osiAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_OA_SUBSCRIPTION_ID (" + ZTE_OA_SUBSCRIPTION_ID + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_OA_SUBSCRIPTION_ID (" + ZTE_OA_SUBSCRIPTION_ID + ")");
		}
		
		return osiAvp;
	}
	
	public static Avp getDaSubscriptionId(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		}
		
		Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		if (ismpAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		}
		
		Avp osiAvp = getSubAvp(ismpAvp, ZTE_DA_SUBSCRIPTION_ID);
		if (osiAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_DA_SUBSCRIPTION_ID (" + ZTE_DA_SUBSCRIPTION_ID + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_DA_SUBSCRIPTION_ID (" + ZTE_DA_SUBSCRIPTION_ID + ")");
		}
		
		return osiAvp;
	}
	
	public static int getOaSubscriptionIdType(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		}
		
		Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		if (ismpAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		}
		
		Avp osiAvp = getSubAvp(ismpAvp, ZTE_OA_SUBSCRIPTION_ID);
		if (osiAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_OA_SUBSCRIPTION_ID (" + ZTE_OA_SUBSCRIPTION_ID + ")");
		
		Avp subscriptionIdTypeAvp = getSubAvp(osiAvp, SubscriptionIdTypeAvp.AVP_CODE);
		if (subscriptionIdTypeAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: OA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		
		try {
			return subscriptionIdTypeAvp.getAsInt();
		} catch (AvpDataException e) {
			LogService.appLog.error("Semantic Error deep inside the DIAMETER Stack. Context: OA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		    throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: OA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
	}
	
	public static int getDaSubscriptionIdType(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		}
		
		Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		if (ismpAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		}
		
		Avp osiAvp = getSubAvp(ismpAvp, ZTE_DA_SUBSCRIPTION_ID);
		if (osiAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_DA_SUBSCRIPTION_ID (" + ZTE_DA_SUBSCRIPTION_ID + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_DA_SUBSCRIPTION_ID (" + ZTE_DA_SUBSCRIPTION_ID + ")");
		}
		
		Avp subscriptionIdTypeAvp = getSubAvp(osiAvp, SubscriptionIdTypeAvp.AVP_CODE);
		if (subscriptionIdTypeAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: DA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: DA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
		
		try {
			return subscriptionIdTypeAvp.getAsInt();
		} catch (AvpDataException e) {
			LogService.appLog.error("Semantic Error deep inside the DIAMETER Stack. Context: DA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: DA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
	}
	
	public static String getOaSubscriptionIdData(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		}
		
		Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		if (ismpAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		}
		
		Avp osiAvp = getSubAvp(ismpAvp, ZTE_OA_SUBSCRIPTION_ID);
		if (osiAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_OA_SUBSCRIPTION_ID (" + ZTE_OA_SUBSCRIPTION_ID + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_OA_SUBSCRIPTION_ID (" + ZTE_OA_SUBSCRIPTION_ID + ")");
		}
		
		Avp subscriptionIdDataAvp = getSubAvp(osiAvp, SubscriptionIdDataAvp.AVP_CODE);
		if (subscriptionIdDataAvp == null)  {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: OA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: OA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
		
		try {
			return subscriptionIdDataAvp.getAsUTF8String();
		} catch (AvpDataException e) {
		    LogService.appLog.error("Semantic Error deep inside the DIAMETER Stack. Context: OA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: OA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
	}
	
	public static String getDaSubscriptionIdData(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		if (ismpAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		}
		
		Avp osiAvp = getSubAvp(ismpAvp, ZTE_DA_SUBSCRIPTION_ID);
		if (osiAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_DA_SUBSCRIPTION_ID (" + ZTE_DA_SUBSCRIPTION_ID + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_DA_SUBSCRIPTION_ID (" + ZTE_DA_SUBSCRIPTION_ID + ")");
		}
		
		Avp subscriptionIdDataAvp = getSubAvp(osiAvp, SubscriptionIdDataAvp.AVP_CODE);
		if (subscriptionIdDataAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: DA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: DA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
		
		try {
			return subscriptionIdDataAvp.getAsUTF8String();
		} catch (AvpDataException e) {
		    LogService.appLog.error("Semantic Error deep inside the DIAMETER Stack. Context: DA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: DA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
	}
	
	public static int getServiceEnablerType(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		}
		
		Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		if (ismpAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		}
		
		Avp serviceEnablerTypeAvp = getSubAvp(ismpAvp, ZTE_SERVICE_ENABLER_TYPE);
		if (serviceEnablerTypeAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_ENABLER_TYPE (" + ZTE_SERVICE_ENABLER_TYPE + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_ENABLER_TYPE (" + ZTE_SERVICE_ENABLER_TYPE + ")");
		}
		
		try {
			return serviceEnablerTypeAvp.getAsInt();
		} catch (AvpDataException e) {
		    LogService.appLog.error("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_SERVICE_ENABLER_TYPE (" + ZTE_SERVICE_ENABLER_TYPE + ")");
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_SERVICE_ENABLER_TYPE (" + ZTE_SERVICE_ENABLER_TYPE + ")");
		}
	}
	
	public static int getMessageId(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		}
		
		Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		if (ismpAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		}
		
		Avp messageIdAvp = getSubAvp(ismpAvp, ZTE_MESSAGE_ID);
		if (messageIdAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_MESSAGE_ID (" + ZTE_MESSAGE_ID + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_MESSAGE_ID (" + ZTE_MESSAGE_ID + ")");
		}
		
		try {
			return messageIdAvp.getAsInt();
		} catch (AvpDataException e) {
			LogService.appLog.error("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_MESSAGE_ID (" + ZTE_MESSAGE_ID + ")");
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_MESSAGE_ID (" + ZTE_MESSAGE_ID + ")");
		}
	}
	
	public static String getSpId(MmsDccCharge dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		}
		
		Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
		if (ismpAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		}
		
		Avp spIdAvp = getSubAvp(ismpAvp, ZTE_SP_ID);
		if (spIdAvp == null) {
		    LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SP_ID (" + ZTE_SP_ID + ")");
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SP_ID (" + ZTE_SP_ID + ")");
		}
		
		try {
			return spIdAvp.getAsUTF8String();
		} catch (AvpDataException e) {
			LogService.appLog.error("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_MESSAGE_ID (" + ZTE_SP_ID + ")");
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_MESSAGE_ID (" + ZTE_SP_ID + ")");
		}
	}

    public static String getChargingType(MmsDccCharge dccRequest) throws ServiceLogicException {
        Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
        if (serviceInformation == null) {
            LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
            throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
        }
        
        Avp ismpAvp = getSubAvp(serviceInformation, ZTE_ISMP_INFORMATION);
        if (ismpAvp == null) {
            LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
            throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
        }
        
        Avp ctAvp = getSubAvp(ismpAvp, ZTE_CHARGING_TYPE);
        if (ctAvp == null) {
            LogService.appLog.error("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_CHARGING_TYPE (" + ZTE_CHARGING_TYPE + ")");
            throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_CHARGING_TYPE (" + ZTE_CHARGING_TYPE + ")");
        }
        
        try {
            return ctAvp.getAsUTF8String();
        } catch (AvpDataException e) {
            LogService.appLog.error("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_CHARGING_TYPE (" + ZTE_CHARGING_TYPE + ")");
            throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_CHARGING_TYPE (" + ZTE_CHARGING_TYPE + ")");
        }
    }
	
}
