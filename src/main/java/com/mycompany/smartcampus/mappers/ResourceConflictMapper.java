/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.mappers;
import com.mycompany.smartcampus.Errormodels.ErrorResponse;
import com.mycompany.smartcampus.exceptions.ResourceConflictException;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
/**
 *
 * @author wenuk
 */
@Provider
public class ResourceConflictMapper implements ExceptionMapper<ResourceConflictException> {

    @Override
    public Response toResponse(ResourceConflictException ex) {
        
        ErrorResponse error = new ErrorResponse( ex.getMessage(), 409);
              
        return Response.status(Response.Status.CONFLICT) 
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
