/*
 * Desafio Tecnico Topaz
 * Desenvolvido por L.A.Leandro
 * São José dos Campos - SP
 * 18-04-2026
 */
package com.topaz.urlshortener.rest;

import com.topaz.urlshortener.entity.UrlMapping;
import com.topaz.urlshortener.service.UrlService;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Path("")
public class RedirectResource {
    
    @Inject
    private UrlService urlService;
    
    @GET
    @Path("/{shortCode}")
    public Response redirect(@PathParam("shortCode") String shortCode) {
        try {
            UrlMapping url = urlService.findByShortCode(shortCode);
            if (url == null) {
                url = urlService.findByAlias(shortCode);
            }
            
            if (url == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorResponse("Código não encontrado"))
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            
            urlService.incrementAccessCount(shortCode);
            
            return Response.seeOther(new URI(url.getOriginalUrl())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse(e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
    
    @GET
    @Path("/api/info/{shortCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfo(@PathParam("shortCode") String shortCode) {
        try {
            UrlMapping url = urlService.findByShortCode(shortCode);
            if (url == null) {
                url = urlService.findByAlias(shortCode);
            }
            
            if (url == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorResponse("Código não encontrado"))
                        .build();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("shortUrl", "http://localhost:8080/" + url.getShortCode());
            result.put("shortCode", url.getShortCode());
            result.put("originalUrl", url.getOriginalUrl());
            result.put("alias", url.getAlias());
            result.put("accessCount", url.getAccessCount());
            
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse(e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/api/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "healthy");
        result.put("timestamp", System.currentTimeMillis());
        return Response.ok(result).build();
    }
    
    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}