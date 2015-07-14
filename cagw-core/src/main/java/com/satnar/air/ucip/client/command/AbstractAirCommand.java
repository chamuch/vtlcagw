package com.satnar.air.ucip.client.command;

public abstract class AbstractAirCommand<T> implements Command<T> {
	
	protected String endpointId;

	public void setEndpointId(String endpointId) {
		this.endpointId = endpointId;
	}
}
