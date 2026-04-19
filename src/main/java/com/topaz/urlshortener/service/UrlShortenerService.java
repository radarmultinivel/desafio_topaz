/*
 * Desafio Tecnico Topaz
 * URL Shortener - Service
 * Padrão: Service com lógica de negócio e sincronização
 * Desenvolvedor: L.A.Leandro
 * Data: 18-04-2026
 * Local: São José dos Campos - SP
 */
package com.topaz.urlshortener.service;

import com.topaz.urlshortener.dto.ShortenResponseDTO;
import com.topaz.urlshortener.entity.UrlMapping;
import com.topaz.urlshortener.repository.UrlMappingRepository;
import com.topaz.urlshortener.util.Base62;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class UrlShortenerService {

    @Inject
    private UrlMappingRepository repository;

    private final AtomicLong counter = new AtomicLong(0);
    private final Object lock = new Object();

    public ShortenResponseDTO shortenUrl(String url, String alias) {
        if (url == null || url.trim().isEmpty()) {
            return ShortenResponseDTO.error("URL obrigatória");
        }

        if (alias != null && !alias.trim().isEmpty()) {
            if (!isValidAlias(alias)) {
                return ShortenResponseDTO.error("Alias inválido. Use apenas letras e números (3-20 caracteres).");
            }

            if (repository.existsByAlias(alias)) {
                return ShortenResponseDTO.error("Alias já está em uso. Escolha outro.");
            }

            return createUrlMapping(url, alias);
        }

        String shortCode = generateUniqueShortCode();
        return createUrlMapping(url, shortCode);
    }

    private ShortenResponseDTO createUrlMapping(String url, String shortCode) {
        UrlMapping entity = new UrlMapping(url, shortCode);
        repository.save(entity);

        return ShortenResponseDTO.success(
                "http://localhost:8080/" + shortCode,
                shortCode,
                url,
                entity.getAlias(),
                0L
        );
    }

    public String findOriginalUrl(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(url -> {
                    url.setAccessCount(url.getAccessCount() + 1);
                    repository.update(url);
                    return url.getOriginalUrl();
                })
                .orElse(null);
    }

    public ShortenResponseDTO getInfo(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(url -> ShortenResponseDTO.success(
                        "http://localhost:8080/" + url.getShortCode(),
                        url.getShortCode(),
                        url.getOriginalUrl(),
                        url.getAlias(),
                        url.getAccessCount()
                ))
                .orElse(ShortenResponseDTO.error("Código não encontrado"));
    }

    public boolean aliasExists(String alias) {
        return repository.existsByAlias(alias);
    }

    private boolean isValidAlias(String alias) {
        return alias.length() >= 3 && alias.length() <= 20 && alias.matches("^[a-zA-Z0-9]+$");
    }

    private String generateUniqueShortCode() {
        synchronized (lock) {
            String shortCode;
            int attempts = 0;
            do {
                long id = counter.incrementAndGet();
                shortCode = Base62.encode(id);
                attempts++;
                if (attempts > 100) {
                    throw new RuntimeException("Falha ao gerar código único");
                }
            } while (repository.existsByShortCode(shortCode));
            return shortCode;
        }
    }

    private long incrementAndGet() {
        return counter.incrementAndGet();
    }
}