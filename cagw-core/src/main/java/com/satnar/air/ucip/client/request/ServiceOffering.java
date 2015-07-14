package com.satnar.air.ucip.client.request;

import java.io.Serializable;

public class ServiceOffering extends NativeAirRequest implements Serializable {

	private static final long serialVersionUID = -1162307293609919468L;
	
	private Integer serviceOfferingId;
	private boolean serviceOfferingActiveFlag;

	public Integer getServiceOfferingId() {
		return serviceOfferingId;
	}

	public void setServiceOfferingId(Integer serviceOfferingId) {
		this.serviceOfferingId = serviceOfferingId;
		addParam("serviceOfferingID", serviceOfferingId);
	}

	public boolean isServiceOfferingActiveFlag() {
		return serviceOfferingActiveFlag;
	}

	public void setServiceOfferingActiveFlag(boolean serviceOfferingActiveFlag) {
		this.serviceOfferingActiveFlag = serviceOfferingActiveFlag;
		addParam("serviceOfferingActiveFlag", Boolean.valueOf(serviceOfferingActiveFlag));
	}

	@Override
	public String toString() {
		return "ServiceOffering [serviceOfferingId=" + serviceOfferingId + ", serviceOfferingActiveFlag=" + serviceOfferingActiveFlag + "]";
	}
	
	
}
