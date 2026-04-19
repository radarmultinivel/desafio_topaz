/*
 * Desafio Tecnico Topaz
 * URL Shortener - Repository
 * Padrão: Repository pattern com Persistence Context
 * Desenvolvedor: L.A.Leandro
 * Data: 18-04-2026
 * Local: São José dos Campos - SP
 */
package com.topaz.urlshortener.repository;

import com.topaz.urlshortener.entity.UrlMapping;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UrlMappingRepository {

    @PersistenceContext
    private EntityManager em;

    public UrlMapping save(UrlMapping urlMapping) {
        em.persist(urlMapping);
        em.flush();
        return urlMapping;
    }

    public UrlMapping update(UrlMapping urlMapping) {
        em.merge(urlMapping);
        em.flush();
        return urlMapping;
    }

    public Optional<UrlMapping> findById(Long id) {
        UrlMapping entity = em.find(UrlMapping.class, id);
        return Optional.ofNullable(entity);
    }

    public Optional<UrlMapping> findByShortCode(String shortCode) {
        TypedQuery<UrlMapping> query = em.createNamedQuery("UrlMapping.findByShortCode", UrlMapping.class);
        query.setParameter("shortCode", shortCode);
        List<UrlMapping> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<UrlMapping> findByAlias(String alias) {
        if (alias == null || alias.isEmpty()) {
            return Optional.empty();
        }
        TypedQuery<UrlMapping> query = em.createNamedQuery("UrlMapping.findByAlias", UrlMapping.class);
        query.setParameter("alias", alias);
        List<UrlMapping> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<UrlMapping> findAll() {
        TypedQuery<UrlMapping> query = em.createNamedQuery("UrlMapping.findAll", UrlMapping.class);
        return query.getResultList();
    }

    public boolean existsByShortCode(String shortCode) {
        return findByShortCode(shortCode).isPresent();
    }

    public boolean existsByAlias(String alias) {
        return findByAlias(alias).isPresent();
    }
}