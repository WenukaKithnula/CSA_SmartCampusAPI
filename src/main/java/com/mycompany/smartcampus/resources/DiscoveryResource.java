package com.mycompany.smartcampus.resources;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/") 
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    
    public Response getDiscoveryInfo(@Context UriInfo uriInfo) {
        // Create the main container for our metadata
        Map<String, Object> discovery = new HashMap<>();
        
        discovery.put("version", "v1.0.0");
        discovery.put("description", "Smart Campus API is Live");
        discovery.put("adminContact", "wenuka.20242072@iit.ac.lk");
        
        Map<String, String> links = new HashMap<>();
        
        links.put("rooms", uriInfo.getBaseUriBuilder().path(SensorRoomResource.class).build().toString());
        links.put("sensors", uriInfo.getBaseUriBuilder().path(SensorResource.class).build().toString());
        
        discovery.put("links", links);

        return Response.ok(discovery).build();
        
       //Versioning info
        
    }
}