/*
 * Desafio Tecnico Topaz
 * JAX-RS Application Configuration
 * Desenvolvedor: L.A.Leandro
 * Data: 18-04-2026
 * Local: São José dos Campos - SP
 */
package com.topaz.urlshortener.resource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class JAXRSApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(UrlShortenerResource.class);
        return classes;
    }
}