# URL Shortener - Desafio Técnico Topaz

## Visão Geral

Este projeto é um **Encurtador de URLs** desenvolvido como desafio técnico para a empresa Topaz.

**Objetivo:** Demonstrar competências em Java 8, Java EE 7, JAX-RS, JPA, CDI e boas práticas de desenvolvimento.

---

## Stack Tecnológica

| Tecnologia | Versão | Justificativa |
|-----------|--------|-----------|
| Java | 8 | Requisito do desafio (stack da empresa) |
| JAX-RS | 2.0 | API REST padrão Java EE 7 |
| JPA + Hibernate | 5.1 | Persistência com ORM |
| CDI | 1.2 | Injeção de dependência |
| WildFly | 10 | Servidor de aplicação Java EE 7 |
| H2 | 1.4 | Banco em memória para testes |

---

## Arquitetura

O projeto segue **Arquitetura em Camadas**:

```
src/main/java/com/topaz/urlshortener/
├── dto/              # Data Transfer Objects
│   ├── ShortenRequestDTO.java
│   └── ShortenResponseDTO.java
├── entity/            # Entidades JPA
│   └── UrlMapping.java
├── repository/        # Camada de persistência
│   └── UrlMappingRepository.java
├── resource/          # API JAX-RS
│   ├── UrlShortenerResource.java
│   └── JAXRSApplication.java
└── service/          # Lógica de negócio
    └── UrlShortenerService.java
```

### Decisões de Design

1. **Padrão Repository:** Isola a lógica de persistência, facilitando testes e manutenção.

2. **Padrão Service:** Contém a lógica de negócio e coordenação entre Repository e Resource.

3. **DTOs:** Separação entreAPI e modelo interno. Permite evoluir independently.

4. **Base62 para shortcodes:** 
   - Usa caracteres alfanuméricos (0-9, a-z, A-Z) = 62 caracteres
   - Gera códigos curtos e amigáveis
   - Exemplo: ID 1000 → "sC" (em Base62)

---

## API REST

### Endpoints

| Método | Path | Descrição |
|--------|------|----------|
| POST | `/api/shorten` | Encurtar URL |
| GET | `/{code}` | Redirect para URL original |
| GET | `/api/info/{code}` | Info da URL encurtada |
| GET | `/api/health` | Health check |

### Exemplos de Uso

```bash
# Encurtar URL (form-urlencoded)
curl -X POST "http://localhost:8080/api/shorten/form" \
  -d "url=https://google.com" \
  -d "alias=google"

# Resposta
{
  "shortUrl": "http://localhost:8080/google",
  "shortCode": "google",
  "originalUrl": "https://google.com",
  "alias": "google",
  "accessCount": 0
}

# Redirect
curl -v "http://localhost:8080/google"
# HTTP/1.1 303 See Other
# Location: https://google.com
```

---

## Como Executar

### Pré-requisitos

- Java 8 JDK
- Maven 3.x
- WildFly 10 (ou superior)

### Build

```bash
mvn clean package
```

### Deploy no WildFly

1. Copie o WAR para `wildfly-10.0.0.Final/standalone/deployments/`:
   ```bash
   cp target/url-shortener.war $WILDFLY_HOME/standalone/deployments/
   ```

2. Inicie o servidor:
   ```bash
   $WILDFLY_HOME/bin/standalone.sh
   ```

3. Acesse: `http://localhost:8080/url-shortener/`

### Modo Desenvolvimento (Jetty)

```bash
mvn jetty:run
```

Acesse: `http://localhost:8080/`

---

## Regras de Negócio

1. **URL obrigatória:** Retorna erro se URL vazia.

2. **Alias opcional:**
   - Se informado: deve ter 3-20 caracteres alfanuméricos
   - Deve ser único
   - Se não informado: gera shortcode automático

3. **Redirect:** HTTP 303 (See Other)

4. **Sincronização:** Bloco `synchronized` para garantir único shortcode em cenários de concorrência

---

## Testes

```bash
# Executar testes
mvn test

# Compilar sem testes
mvn clean package -Dmaven.test.skip=true
```

---

## Trade-offs e Melhorias Futuras

Com mais tempo, as seguintes melhorias seriam implementadas:

| Melhoria | Justificativa |
|---------|-------------|
| Cache Redis | Reduzir latência em reads frequentes |
| Métricas de cliques | Analytics por URL |
| QR Code | Geração automática |
| Expiração de URLs | URLs temporárias |
| Autenticação | Controle de acesso |
| PostgreSQL | Persistência em produção |

---

## Autor

- **Desenvolvedor:** L.A.Leandro
- **Data:** 18-04-2026
- **Local:** São José dos Campos - SP