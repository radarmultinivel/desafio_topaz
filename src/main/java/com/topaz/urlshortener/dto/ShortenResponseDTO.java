/*
 * Desafio Tecnico Topaz
 * URL Shortener - DTO de resposta
 * Desenvolvedor: L.A.Leandro
 * Data: 18-04-2026
 * Local: São José dos Campos - SP
 */
package com.topaz.urlshortener.dto;

import java.io.Serializable;

public class ShortenResponseDTO implements Serializable {

    private String shortUrl;
    private String shortCode;
    private String originalUrl;
    private String alias;
    private Long accessCount;
    private String error;

    public ShortenResponseDTO() {
    }

    public static ShortenResponseDTO success(String shortUrl, String shortCode, String originalUrl, String alias, Long accessCount) {
        ShortenResponseDTO dto = new ShortenResponseDTO();
        dto.shortUrl = shortUrl;
        dto.shortCode = shortCode;
        dto.originalUrl = originalUrl;
        dto.alias = alias;
        dto.accessCount = accessCount;
        return dto;
    }

    public static ShortenResponseDTO error(String error) {
        ShortenResponseDTO dto = new ShortenResponseDTO();
        dto.error = error;
        return dto;
    }

    // Getters
    public String getShortUrl() {
        return shortUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getAlias() {
        return alias;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public String getError() {
        return error;
    }
}