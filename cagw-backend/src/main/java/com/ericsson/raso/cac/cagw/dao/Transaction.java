package com.ericsson.raso.cac.cagw.dao;

public class Transaction {
	
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
	public String getAccountType() {
        return accountType;
    }
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
    public boolean isChargeStatus() {
		return chargeStatus;
	}
	public void setChargeStatus(boolean chargeStatus) {
		this.chargeStatus = chargeStatus;
	}
	public long getTransactionTime() {
		return transactionTime;
	}
	public void setTransactionTime(long transactionTime) {
		this.transactionTime = transactionTime;
	}
    @Override
    public String toString() {
        return String
                .format("Transaction [transactionTime=%s, transactionId=%s, messageId=%s, chargingSessionId=%s, sourceAddress=%s, destinationAddress=%s, chargedParty=%s, accountId=%s, amount=%s, accountType=%s, chargeStatus=%s]",
                        transactionTime,
                        transactionId,
                        messageId,
                        chargingSessionId,
                        sourceAddress,
                        destinationAddress,
                        chargedParty,
                        accountId,
                        amount,
                        accountType,
                        chargeStatus);
    }
	
	
}
