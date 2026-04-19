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
import java.util.HashMap;
import java.util.Map;

@Path("/shorten")
public class UrlShortenerResource {
    
    @Inject
    private UrlService urlService;
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response shortenUrl(@FormParam("url") String url,
                          @FormParam("alias") String alias) {
        try {
            if (url == null || url.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse("URL obrigatória"))
                        .build();
            }
            
            String shortCode;
            if (alias != null && !alias.trim().isEmpty()) {
                if (alias.length() < 3 || alias.length() > 20 || !alias.matches("^[a-zA-Z0-9]+$")) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(errorResponse("Alias inválido"))
                            .build();
                }
                if (urlService.aliasExists(alias)) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(errorResponse("Alias já em uso"))
                            .build();
                }
                shortCode = alias;
            } else {
                shortCode = urlService.generateUniqueShortCode();
            }
            
            UrlMapping urlMapping = new UrlMapping(url, shortCode);
            if (alias != null && !alias.trim().isEmpty()) {
                urlMapping.setAlias(alias);
            }
            
            urlService.save(urlMapping);
            
            Map<String, Object> result = new HashMap<>();
            result.put("shortUrl", "http://localhost:8080/" + shortCode);
            result.put("shortCode", shortCode);
            result.put("originalUrl", url);
            result.put("alias", alias);
            result.put("accessCount", 0);
            
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse(e.getMessage()))
                    .build();
        }
    }
    
    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}