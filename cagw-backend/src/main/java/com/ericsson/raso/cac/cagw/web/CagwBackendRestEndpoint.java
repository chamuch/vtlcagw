package com.ericsson.raso.cac.cagw.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/CgWeb/")
public class CagwBackendRestEndpoint {

	//private static Logger logger = LoggerFactory.getLogger(CagwBackendRestEndpoint.class);
	@POST
	@Path("/process/")
	//@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes("application/json")
	public void process(byte[] object) {
		//logger.debug("Reached Inside the process method of CgWeb");
	}
}

