/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.mappers;
import com.mycompany.smartcampus.Errormodels.ErrorResponse;
import com.mycompany.smartcampus.exceptions.SensorUnavailableException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
/**
 *
 * @author wenuk
 */
@Provider
public class SensorUnavailableMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), 403);
        return Response.status(Response.Status.FORBIDDEN)
                       .entity(error)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
