/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;
import com.mycompany.smartcampus.models.Sensor;
import com.mycompany.smartcampus.Errormodels.ErrorResponse;
import com.mycompany.smartcampus.service.RoomService;
import com.mycompany.smartcampus.service.SensorService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;


/**
 *
 * @author wenuk
 */
@Path("/sensors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {
    private final SensorService sensorService = new SensorService();

    @POST
    
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Input Validation
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
           ErrorResponse error = new ErrorResponse("Sensor ID missing", 400);
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        if (sensor.getRoomId() == null ||sensor.getRoomId().trim().isEmpty()) {
           ErrorResponse error = new ErrorResponse("Room ID parameter is missing", 400);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        
        sensorService.registerSensor(sensor);

        // Build the URI for the Location header
        java.net.URI location = uriInfo.getAbsolutePathBuilder()
                                      .path(sensor.getId())
                                      .build();

        return Response.created(location).entity(sensor).build();
    }
    
   @GET
  
   public Response getAllSensors(@QueryParam("type") String type) {
       
       //this will return the list of all the sensors filters depening on the request 
       List<Sensor> sensors = sensorService.getSensors(type);

       if (sensors.isEmpty()) {
           // Return 204 No Content if the filter results in an empty list
           return Response.noContent().build();
       }

       return Response.ok(sensors).build();
   }
   
   
   //reading a specif sensor 
   @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
   
    return new SensorReadingResource(sensorId);
}
}