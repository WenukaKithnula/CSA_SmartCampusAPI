/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;
import com.mycompany.smartcampus.models.SensorReading;
import com.mycompany.smartcampus.service.SensorReadingService;
import com.mycompany.smartcampus.Errormodels.ErrorResponse;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author wenuk
 */
public class SensorReadingResource {
    
    private final String sensorId;
    private final SensorReadingService sensorReadingService = new SensorReadingService();
    
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHistory() {
        List<SensorReading> readings = sensorReadingService.getReadingsForSensor(sensorId);
        
        // If no readings exist, 204 No Content is semantically better than an empty list
        if (readings == null || readings.isEmpty()) {
            return Response.noContent().build();
        }
        
        return Response.ok(readings).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        // This calls the service logic we wrote above
        if (reading == null) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(new ErrorResponse("Reading body is required.", 400))
                       .build();
    }
        sensorReadingService.addReadingToSensor(sensorId, reading);
        
        return Response.status(Response.Status.CREATED) // Status 201
                       .entity(reading)
                       .build();
    }
}
