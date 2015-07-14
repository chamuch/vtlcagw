package com.satnar.air.ucip.client.command;

import com.satnar.air.ucip.client.UcipException;

public interface Command<T> {
	
	T execute() throws UcipException;

}
