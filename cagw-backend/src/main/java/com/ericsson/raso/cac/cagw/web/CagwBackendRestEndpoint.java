package com.ericsson.raso.cac.cagw.web;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Path("/CgWeb/")
public class CagwBackendRestEndpoint {

	//private static Logger logger = LoggerFactory.getLogger(CagwBackendRestEndpoint.class);
	@POST
	@Path("/process/{fe}")
	//@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public void process(byte[] object) {
		
		//logger.debug("Reached Inside the process method of CgWeb");
	}
}

