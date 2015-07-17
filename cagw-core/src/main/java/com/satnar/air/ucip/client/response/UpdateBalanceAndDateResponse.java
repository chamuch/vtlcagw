package com.satnar.air.ucip.client.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateBalanceAndDateResponse extends AbstractAirResponse {

	private static final long serialVersionUID = 1L;
	
	private List<DedicatedAccountChangeInformation> dedicatedAccountInformation;

	@SuppressWarnings("unchecked")
	public List<DedicatedAccountChangeInformation> getDedicatedAccountInformation() {
		if (dedicatedAccountInformation == null) {
			dedicatedAccountInformation = new ArrayList<DedicatedAccountChangeInformation>();
			Object[] DedicatedAccountInformationRes = (Object[]) (((Map<?, ?>) result)
					.get("dedicatedAccountChangeInformation"));
			if(DedicatedAccountInformationRes != null) {
				for (Object oi : DedicatedAccountInformationRes) {
					dedicatedAccountInformation.add(new DedicatedAccountChangeInformation((Map<String, Object>) oi));
				}
			}
		}
		return dedicatedAccountInformation;
	}

    @Override
    public String toString() {
        return String.format("UpdateBalanceAndDateResponse [getResponseCode()=%s, isResponseAvailable()=%s, getResult()=%s, dedicatedAccountInformation=%s]",
                getResponseCode(),
                isResponseAvailable(),
                getResult(),
                dedicatedAccountInformation);
    }
	
	

}
