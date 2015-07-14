package com.ericsson.raso.cac.cagw.dao;

public class Archive {
	
	private long transactionTime;
	private int transactionId;
	private String messageId;
	private String chargingSessionId;
	private String sourceAddress;
	private String destinationAddress;
	private String chargedParty;
	private String accountId;
	private String amount;
	private String accountType;
    private boolean chargeStatus;
	private boolean deliveryStatus;
	private boolean refundStatus;
	private long refundTime;
	
	
	public Archive(Transaction transaction) {
	    this.transactionTime = transaction.getTransactionTime();
	    this.transactionId = transaction.getTransactionId();
	    this.messageId = transaction.getMessageId();
	    this.chargingSessionId = transaction.getChargingSessionId();
	    this.sourceAddress = transaction.getSourceAddress();
	    this.destinationAddress = transaction.getDestinationAddress();
	    this.chargedParty = transaction.getChargedParty();
	    this.chargeStatus = transaction.isChargeStatus();	
	    this.accountId = transaction.getAccountId();
	    this.amount = transaction.getAmount();
	    this.accountType = transaction.getAccountType();
    }
	
		
	public int getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getChargingSessionId() {
		return chargingSessionId;
	}
	public void setChargingSessionId(String chargingSessionId) {
		this.chargingSessionId = chargingSessionId;
	}
	public String getSourceAddress() {
		return sourceAddress;
	}
	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}
	public String getDestinationAddress() {
		return destinationAddress;
	}
	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}
	public String getChargedParty() {
		return chargedParty;
	}
	public void setChargedParty(String chargedParty) {
		this.chargedParty = chargedParty;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public boolean isChargeStatus() {
		return chargeStatus;
	}
	public String getAmount() {
        return amount;
    }


    public void setAmount(String amount) {
        this.amount = amount;
    }


    public String getAccountType() {
        return accountType;
    }


    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }


    public void setChargeStatus(boolean chargeStatus) {
		this.chargeStatus = chargeStatus;
	}
	public boolean isDeliveryStatus() {
		return deliveryStatus;
	}
	public void setDeliveryStatus(boolean deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}
	public boolean isRefundStatus() {
		return refundStatus;
	}
	public void setRefundStatus(boolean refundStatus) {
		this.refundStatus = refundStatus;
	}
	public long getTransactionTime() {
		return transactionTime;
	}
	public void setTransactionTime(long transactionTime) {
		this.transactionTime = transactionTime;
	}
	public long getRefundTime() {
		return refundTime;
	}
	public void setRefundTime(long refundTime) {
		this.refundTime = refundTime;
	}	
}
