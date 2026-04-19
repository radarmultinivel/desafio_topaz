/*
 * Desafio Tecnico Topaz
 * Desenvolvido por L.A.Leandro
 * São José dos Campos - SP
 * 18-04-2026
 */
package com.topaz.urlshortener.service;

import com.topaz.urlshortener.entity.UrlMapping;
import com.topaz.urlshortener.util.Base62;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Named
@ApplicationScoped
public class UrlService {
    
    @PersistenceContext
    private EntityManager em;
    
    private final AtomicLong counter = new AtomicLong(0);
    
    @Transactional
    public void save(UrlMapping urlMapping) {
        em.persist(urlMapping);
    }
    
    @Transactional
    public UrlMapping findByShortCode(String shortCode) {
        return em.createNamedQuery("UrlMapping.findByShortCode", UrlMapping.class)
                .setParameter("shortCode", shortCode)
                .getSingleResult();
    }
    
    @Transactional
    public UrlMapping findByAlias(String alias) {
        return em.createNamedQuery("UrlMapping.findByAlias", UrlMapping.class)
                .setParameter("alias", alias)
                .getSingleResult();
    }
    
    @Transactional
    public List<UrlMapping> findAll() {
        return em.createNamedQuery("UrlMapping.findAll", UrlMapping.class).getResultList();
    }
    
    @Transactional
    public void incrementAccessCount(String shortCode) {
        UrlMapping url = findByShortCode(shortCode);
        if (url != null) {
            url.setAccessCount(url.getAccessCount() + 1);
            em.merge(url);
        }
    }
    
    public String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;
        do {
            long id = counter.incrementAndGet();
            shortCode = Base62.encode(id);
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Failed to generate unique code");
            }
        } while (shortCodeExists(shortCode));
        return shortCode;
    }
    
    public boolean shortCodeExists(String shortCode) {
        try {
            findByShortCode(shortCode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean aliasExists(String alias) {
        if (alias == null || alias.isEmpty()) {
            return false;
        }
        try {
            findByAlias(alias);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}