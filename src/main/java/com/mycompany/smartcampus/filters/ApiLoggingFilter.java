/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.filters;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 *
 * @author wenuk
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    // Fires on every incoming request
    @Override
    public void filter(ContainerRequestContext requestContext) {
        LOGGER.info(">>> Incoming Request: ["
                + requestContext.getMethod() + "] "
                + requestContext.getUriInfo().getRequestUri());
    }

    // Fires on every outgoing response
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        LOGGER.info("<<< Outgoing Response: Status " 
                + responseContext.getStatus()
                + " for [" + requestContext.getMethod() + "] "
                + requestContext.getUriInfo().getRequestUri());
    }
}
