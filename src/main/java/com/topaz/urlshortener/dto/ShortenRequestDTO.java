/*
 * Desafio Tecnico Topaz
 * URL Shortener - DTOs para API
 * Desenvolvedor: L.A.Leandro
 * Data: 18-04-2026
 * Local: São José dos Campos - SP
 */
package com.topaz.urlshortener.dto;

import java.io.Serializable;

public class ShortenRequestDTO implements Serializable {

    private String url;
    private String alias;

    public ShortenRequestDTO() {
    }

    public ShortenRequestDTO(String url, String alias) {
        this.url = url;
        this.alias = alias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}