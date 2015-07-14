package com.satnar.air.ucip.client.request;

public class ChargingRequestInformation extends NativeAirRequest {
	private Integer chargingType;
	private Integer chargingIndictor;
	private Integer reservationCorrelationID;

	public Integer getChargingType() {
		return chargingType;
	}

	public Integer getChargingIndictor() {
		return chargingIndictor;
	}

	public Integer getReservationCorrelationID() {
		return reservationCorrelationID;
	}

	public void setChargingType(Integer chargingType) {
		this.chargingType = chargingType;
		addParam("chargingType", this.chargingType);
	}

	public void setChargingIndictor(Integer chargingIndictor) {
		this.chargingIndictor = chargingIndictor;
		addParam("chargingIndictor", this.chargingIndictor);
	}

	public void setReservationCorrelationID(Integer reservationCorrelationID) {
		this.reservationCorrelationID = reservationCorrelationID;
		addParam("reservationCorrelationID", this.reservationCorrelationID);
	}

}
