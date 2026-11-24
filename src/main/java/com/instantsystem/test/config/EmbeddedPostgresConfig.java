package com.instantsystem.test.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Démarre une instance PostgreSQL embarquée (binaire réel via Zonky) au lancement
 * de l'application et l'expose comme DataSource primaire. L'instance est éphémère :
 * elle est détruite à l'arrêt de l'application, les données ne sont pas persistées.
 * Activée par défaut ; désactivable avec {@code embedded.postgres.enabled=false}
 * pour brancher un vrai PostgreSQL (prod) via les propriétés spring.datasource.*.
 */
@Configuration
@ConditionalOnProperty(name = "embedded.postgres.enabled", havingValue = "true", matchIfMissing = true)
public class EmbeddedPostgresConfig {

    /** Port fixe pour pouvoir s'y connecter (psql/DBeaver), configurable via application.properties. */
    @Value("${embedded.postgres.port:5432}")
    private int port;

    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() throws IOException {
        return EmbeddedPostgres.builder()
                .setPort(port)
                .start();
    }

    @Bean
    @Primary
    public DataSource dataSource(EmbeddedPostgres embeddedPostgres) {
        return embeddedPostgres.getPostgresDatabase();
    }
}
