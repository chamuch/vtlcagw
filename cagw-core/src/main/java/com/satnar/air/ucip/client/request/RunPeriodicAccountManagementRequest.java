package com.satnar.air.ucip.client.request;




public class RunPeriodicAccountManagementRequest extends AbstractAirRequest {
	
    private Integer pamServiceID;
    private Integer pamIndicator;
    
	public RunPeriodicAccountManagementRequest() {
		super("RunPeriodicAccountManagement");
	}
	
	public Integer getPamServiceID() {
		return pamServiceID;
	}

	public void setPamServiceID(Integer pamServiceID) {
		this.pamServiceID = pamServiceID;
		addParam("pamServiceID", pamServiceID);
	}
	
	public Integer getPamIndicator() {
		return pamIndicator;
	}

	public void setPamIndicator(Integer pamIndicator) {
		this.pamIndicator = pamIndicator;
		addParam("pamIndicator", pamIndicator);
	}	

}
