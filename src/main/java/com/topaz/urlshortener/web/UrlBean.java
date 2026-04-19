/*
 * Desafio Tecnico Topaz
 * Desenvolvido por L.A.Leandro
 * São José dos Campos - SP
 * 18-04-2026
 */
package com.topaz.urlshortener.web;

import com.topaz.urlshortener.entity.UrlMapping;
import com.topaz.urlshortener.service.UrlService;
import com.topaz.urlshortener.util.Base62;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@javax.enterprise.context.SessionScoped
public class UrlBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private UrlService urlService;
    
    private String originalUrl;
    private String alias;
    private String shortUrl;
    private String shortCode;
    private Long accessCount;
    
    @PostConstruct
    public void init() {
    }
    
    public String shortenUrl() {
        try {
            if (originalUrl == null || originalUrl.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "URL é obrigatória", null));
                return null;
            }
            
            String code;
            if (alias != null && !alias.trim().isEmpty()) {
                if (alias.length() < 3 || alias.length() > 20 || !alias.matches("^[a-zA-Z0-9]+$")) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Alias inválido", null));
                    return null;
                }
                code = alias;
            } else {
                code = urlService.generateUniqueShortCode();
            }
            
            UrlMapping urlMapping = new UrlMapping(originalUrl, code);
            if (alias != null && !alias.trim().isEmpty()) {
                urlMapping.setAlias(alias);
            }
            
            urlService.save(urlMapping);
            
            shortUrl = "http://localhost:8080/" + code;
            shortCode = code;
            accessCount = 0L;
            
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "URL shortening successfully!", null));
            
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: " + e.getMessage(), null));
            return null;
        }
    }
    
    public String getOriginalUrl() {
        return originalUrl;
    }
    
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getShortUrl() {
        return shortUrl;
    }
    
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
    
    public String getShortCode() {
        return shortCode;
    }
    
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
    
    public Long getAccessCount() {
        return accessCount;
    }
    
    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }
}