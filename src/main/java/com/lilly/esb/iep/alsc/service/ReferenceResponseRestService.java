/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("masterref/response/v3")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.TEXT_PLAIN)
public class ReferenceResponseRestService {
    
    @POST
    @Path("finalresp")
    public Response finalresp(String body) {
        return null;
    }
}