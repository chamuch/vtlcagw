package com.ericsson.raso.cac.cagw.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Path("/CgWeb/")
public class CagwBackendRestEndpoint {

	//private static Logger logger = LoggerFactory.getLogger(CagwBackendRestEndpoint.class);
	@POST
	@Path("/process/{fe}")
	//@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes("application/json")
	public void process(byte[] object) {
		System.out.println("HowManyTimes...");
		//logger.debug("Reached Inside the process method of CgWeb");
	}
}

