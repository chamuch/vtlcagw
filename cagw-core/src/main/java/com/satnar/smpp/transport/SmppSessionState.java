package com.satnar.smpp.transport;

public enum SmppSessionState {
    INIT_IDLE,
	OPEN,
	BOUND_TX,
	BOUND_RX,
	BOUND_TRX,
	UNBOUND,
	CLOSED;
	
	public boolean isValidNextState(SmppSessionState state) {
		switch (this) {
		    case INIT_IDLE:
		        return (state == OPEN);
            case OPEN:
                return (state == BOUND_RX || 
                        state == BOUND_TRX || 
                        state == BOUND_TX || 
                        state == CLOSED);
            case BOUND_RX:
            case BOUND_TRX:
            case BOUND_TX:
                return (state == UNBOUND || state == CLOSED);
            case UNBOUND:
                return (state == CLOSED);
            case CLOSED:
                return false;
            default:
                return false;
        } 
	}
}
