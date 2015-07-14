package com.ericsson.raso.cac.cagw.processor;

import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdDataAvp;
import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.avpdatatypes.DccGrouped;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.rfcapi.base.avp.AvpDataException;

public class ZteDccHelper {
	
	private static final int	ZTE_SERVICE_INFORMATION	 = 873;
	private static final int	ZTE_ISMP_INFORMATION	 = 20500;
	private static final int	ZTE_MESSAGE_ID	         = 20501;
	private static final int	ZTE_CHARGE_PARTY_TYPE	 = 20502;
	private static final int	ZTE_SP_ID	             = 20504;
	private static final int	ZTE_SERVICE_ENABLER_TYPE = 20505;
	private static final int	ZTE_OA_SUBSCRIPTION_ID	 = 20511;
	private static final int	ZTE_DA_SUBSCRIPTION_ID	 = 20512;
	private static final int    ZTE_CHARGING_TYPE        = 20515;
	
	public static int getTrafficCase(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp chargePartyTypeAvp = ((DccGrouped) ismpAvp).getSubAvp(ZTE_CHARGE_PARTY_TYPE);
		if (chargePartyTypeAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_CHARGE_PARTY_TYPE (" + ZTE_CHARGE_PARTY_TYPE + ")");
		
		try {
			return chargePartyTypeAvp.getAsInt();
		} catch (AvpDataException e) {
			// TODO Log to troubelshoot
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_CHARGE_PARTY_TYPE (" + ZTE_CHARGE_PARTY_TYPE + ")");
		}
	}
	
	public static Avp getServiceInformation(Ccr dccRequest) {
		return dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
	}
	
	public static Avp getServiceInfoIsmp(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		return ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
	}
	
	public static Avp getOaSubscriptionId(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp osiAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_OA_SUBSCRIPTION_ID);
		if (osiAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_OA_SUBSCRIPTION_ID (" + ZTE_OA_SUBSCRIPTION_ID + ")");
		
		return osiAvp;
	}
	
	public static Avp getDaSubscriptionId(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp osiAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_DA_SUBSCRIPTION_ID);
		if (osiAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_DA_SUBSCRIPTION_ID (" + ZTE_DA_SUBSCRIPTION_ID + ")");
		
		return osiAvp;
	}
	
	public static int getOaSubscriptionIdType(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp osiAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_OA_SUBSCRIPTION_ID);
		if (osiAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_OA_SUBSCRIPTION_ID (" + ZTE_OA_SUBSCRIPTION_ID + ")");
		
		Avp subscriptionIdTypeAvp = ((DccGrouped) osiAvp).getSubAvp(SubscriptionIdTypeAvp.AVP_CODE);
		if (subscriptionIdTypeAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: OA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		
		try {
			return subscriptionIdTypeAvp.getAsInt();
		} catch (AvpDataException e) {
			// TODO Log to troubelshoot
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: OA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
	}
	
	public static int getDaSubscriptionIdType(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp osiAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_DA_SUBSCRIPTION_ID);
		if (osiAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_DA_SUBSCRIPTION_ID (" + ZTE_DA_SUBSCRIPTION_ID + ")");
		
		Avp subscriptionIdTypeAvp = ((DccGrouped) osiAvp).getSubAvp(SubscriptionIdTypeAvp.AVP_CODE);
		if (subscriptionIdTypeAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: DA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		
		try {
			return subscriptionIdTypeAvp.getAsInt();
		} catch (AvpDataException e) {
			// TODO Log to troubelshoot
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: DA::SUBSCRIPTION_ID_TYPE (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
	}
	
	public static String getOaSubscriptionIdData(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp osiAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_OA_SUBSCRIPTION_ID);
		if (osiAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_OA_SUBSCRIPTION_ID (" + ZTE_OA_SUBSCRIPTION_ID + ")");
		
		Avp subscriptionIdDataAvp = ((DccGrouped) osiAvp).getSubAvp(SubscriptionIdDataAvp.AVP_CODE);
		if (subscriptionIdDataAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: OA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		
		try {
			return subscriptionIdDataAvp.getAsUTF8String();
		} catch (AvpDataException e) {
			// TODO Log to troubelshoot
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: OA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
	}
	
	public static String getDaSubscriptionIdData(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp osiAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_DA_SUBSCRIPTION_ID);
		if (osiAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_DA_SUBSCRIPTION_ID (" + ZTE_DA_SUBSCRIPTION_ID + ")");
		
		Avp subscriptionIdDataAvp = ((DccGrouped) osiAvp).getSubAvp(SubscriptionIdDataAvp.AVP_CODE);
		if (subscriptionIdDataAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: DA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		
		try {
			return subscriptionIdDataAvp.getAsUTF8String();
		} catch (AvpDataException e) {
			// TODO Log to troubelshoot
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: DA::SUBSCRIPTION_ID_DATA (" + SubscriptionIdTypeAvp.AVP_CODE + ")");
		}
	}
	
	public static int getServiceEnablerType(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp serviceEnablerTypeAvp = ((DccGrouped) ismpAvp).getSubAvp(ZTE_SERVICE_ENABLER_TYPE);
		if (serviceEnablerTypeAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_ENABLER_TYPE (" + ZTE_SERVICE_ENABLER_TYPE + ")");
		
		try {
			return serviceEnablerTypeAvp.getAsInt();
		} catch (AvpDataException e) {
			// TODO Log to troubelshoot
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_SERVICE_ENABLER_TYPE (" + ZTE_SERVICE_ENABLER_TYPE + ")");
		}
	}
	
	public static int getMessageId(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp messageIdAvp = ((DccGrouped) ismpAvp).getSubAvp(ZTE_MESSAGE_ID);
		if (messageIdAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_MESSAGE_ID (" + ZTE_MESSAGE_ID + ")");
		
		try {
			return messageIdAvp.getAsInt();
		} catch (AvpDataException e) {
			// TODO Log to troubelshoot
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_MESSAGE_ID (" + ZTE_MESSAGE_ID + ")");
		}
	}
	
	public static String getSpId(Ccr dccRequest) throws ServiceLogicException {
		Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
		if (serviceInformation == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
		
		Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
		if (ismpAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
		
		Avp spIdAvp = ((DccGrouped) ismpAvp).getSubAvp(ZTE_SP_ID);
		if (spIdAvp == null)
			throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SP_ID (" + ZTE_SP_ID + ")");
		
		try {
			return spIdAvp.getAsUTF8String();
		} catch (AvpDataException e) {
			// TODO Log to troubelshoot
			throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_MESSAGE_ID (" + ZTE_SP_ID + ")");
		}
	}

    public static String getChargingType(Ccr dccRequest) throws ServiceLogicException {
        // TODO DCC-Req->Service-Info->ISMP->ChargingType
        Avp serviceInformation = dccRequest.getAvp(ZTE_SERVICE_INFORMATION);
        if (serviceInformation == null)
            throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_SERVICE_INFORMATION (" + ZTE_SERVICE_INFORMATION + ")");
        
        Avp ismpAvp = ((DccGrouped) serviceInformation).getSubAvp(ZTE_ISMP_INFORMATION);
        if (ismpAvp == null)
            throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_ISMP_INFORMATION (" + ZTE_ISMP_INFORMATION + ")");
        
        Avp ctAvp = ((DccGrouped) ismpAvp).getSubAvp(ZTE_CHARGING_TYPE);
        if (ctAvp == null)
            throw new ServiceLogicException("Requested AVP cannot be found in DCC Request Hierarchy. Missing: ZTE_CHARGING_TYPE (" + ZTE_CHARGING_TYPE + ")");
        
        try {
            return ctAvp.getAsUTF8String();
        } catch (AvpDataException e) {
            // TODO Log to troubelshoot
            throw new ServiceLogicException("Semantic Error deep inside the DIAMETER Stack. Context: ZTE_CHARGING_TYPE (" + ZTE_CHARGING_TYPE + ")");
        }
    }
	
}
