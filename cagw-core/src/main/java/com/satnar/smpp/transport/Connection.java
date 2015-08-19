package com.satnar.smpp.transport;

import java.nio.ByteBuffer;

import com.satnar.smpp.client.ChannelMode;

public abstract class Connection {
	
	private SmppSessionState connectionState = SmppSessionState.INIT_IDLE;
	
	
	public abstract void connect() throws SmppTransportException;
	
	public abstract void disconnect() throws SmppTransportException;
	
	public abstract ByteBuffer getRequestBuffer();
	
	public abstract ByteBuffer getResponseBuffer();
	
	public abstract void write(ByteBuffer writeBuffer) throws SmppTransportException;
	
	public abstract int read(ByteBuffer readBuffer) throws SmppTransportException;
    
    public abstract int getLazyWriteWait();
	
	public abstract int getThreadPoolSize();
	
	public abstract String getEsmeLabel();
	
	public abstract ChannelMode getMode();
	
	public abstract void setMode(ChannelMode mode);
	
    public abstract int getNetworkIdleWaitTime();
	

    public SmppSessionState getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(SmppSessionState newState) throws SmppTransportException {
        if (this.connectionState.isValidNextState(newState))
            this.connectionState = newState;
        else
            throw new SmppTransportException("Illegal State Transition for Connection to promote!!");
    }

	
	
	

}
