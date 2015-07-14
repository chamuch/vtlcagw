package com.ericsson.raso.cac.smpp.viettel;

public interface ISmppEndpoint {

	public abstract boolean isRunning();

	public abstract void start();

	public abstract void stop();

	public abstract int getPhase();

	public abstract boolean isAutoStartup();

	public abstract void stop(Runnable callback);

}