package com.ericsson.raso.cac.cagw.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class DefaultSmppProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		System.out.println("We have entered into DefaultSmppProcessor");
		
	}

}
