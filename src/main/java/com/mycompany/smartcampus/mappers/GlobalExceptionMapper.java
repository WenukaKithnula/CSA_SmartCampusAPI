/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.mappers;

import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mycompany.smartcampus.Errormodels.ErrorResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author wenuk
 */


@Provider 
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable ex) {
        
        
        int statusCode = 500;
        String message = "An unexpected server error occurred.";

        
        if (ex instanceof WebApplicationException) {
            statusCode = ((WebApplicationException) ex).getResponse().getStatus();
            message = ex.getMessage();
        } 
        
       

        //Log the error for your own debugging 
        ex.printStackTrace();

        
        ErrorResponse error = new ErrorResponse(message, statusCode);

        return Response.status(statusCode)
                       .entity(error)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
