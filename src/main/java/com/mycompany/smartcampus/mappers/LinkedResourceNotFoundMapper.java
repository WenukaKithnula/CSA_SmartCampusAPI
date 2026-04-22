/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.mappers;

import com.mycompany.smartcampus.Errormodels.ErrorResponse;
import com.mycompany.smartcampus.exceptions.LinkedResourceNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author wenuk
 */
@Provider
public class LinkedResourceNotFoundMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        // Using your ErrorResponse model for a professional JSON structure
        ErrorResponse errorDetails = new ErrorResponse(ex.getMessage(), 422);

        return Response.status(422) // Unprocessable Entity
                       .entity(errorDetails)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
