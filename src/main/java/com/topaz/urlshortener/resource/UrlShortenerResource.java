/*
 * Desafio Tecnico Topaz
 * URL Shortener - JAX-RS Resource
 * Endpoint REST API
 * Desenvolvedor: L.A.Leandro
 * Data: 18-04-2026
 * Local: São José dos Campos - SP
 */
package com.topaz.urlshortener.resource;

import com.topaz.urlshortener.dto.ShortenRequestDTO;
import com.topaz.urlshortener.dto.ShortenResponseDTO;
import com.topaz.urlshortener.service.UrlShortenerService;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UrlShortenerResource {

    @Inject
    private UrlShortenerService service;

    @POST
    @Path("/api/shorten")
    public Response shortenUrl(ShortenRequestDTO request) {
        try {
            ShortenResponseDTO result = service.shortenUrl(request.getUrl(), request.getAlias());
            if (result.getError() != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(toJson("error", result.getError()))
                        .build();
            }
            return Response.ok(toJson(result)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(toJson("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/api/shorten/form")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response shortenUrlForm(@FormParam("url") String url,
                            @FormParam("alias") String alias) {
        try {
            ShortenResponseDTO result = service.shortenUrl(url, alias);
            if (result.getError() != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(toJson("error", result.getError()))
                        .build();
            }
            return Response.ok(toJson(result)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(toJson("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{shortCode}")
    public Response redirect(@PathParam("shortCode") String shortCode) {
        String originalUrl = service.findOriginalUrl(shortCode);
        if (originalUrl == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(toJson("error", "Código não encontrado"))
                    .build();
        }
        return Response.seeOther(URI.create(originalUrl)).build();
    }

    @GET
    @Path("/api/info/{shortCode}")
    public Response getInfo(@PathParam("shortCode") String shortCode) {
        ShortenResponseDTO result = service.getInfo(shortCode);
        if (result.getError() != null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(toJson("error", result.getError()))
                    .build();
        }
        return Response.ok(toJson(result)).build();
    }

    @GET
    @Path("/api/health")
    public Response health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "healthy");
        result.put("timestamp", System.currentTimeMillis());
        return Response.ok(result).build();
    }

    private Map<String, Object> toJson(ShortenResponseDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("shortUrl", dto.getShortUrl());
        map.put("shortCode", dto.getShortCode());
        map.put("originalUrl", dto.getOriginalUrl());
        map.put("alias", dto.getAlias());
        map.put("accessCount", dto.getAccessCount());
        return map;
    }

    private Map<String, String> toJson(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}