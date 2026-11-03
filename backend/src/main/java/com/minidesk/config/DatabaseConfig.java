package com.minidesk.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * Configuration automatique de la DataSource pour supporter les formats d'URL
 * de bases de données cloud (Render, Railway, Neon, Heroku, etc.).
 *
 * Convertit automatiquement les schémas 'postgres://' et 'postgresql://' en 'jdbc:postgresql://'.
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties() {
            @Override
            public String determineUrl() {
                String rawUrl = super.determineUrl();
                if (!StringUtils.hasText(rawUrl)) {
                    return rawUrl;
                }

                // Normalisation Render / Railway / Supabase
                if (rawUrl.startsWith("postgres://")) {
                    String jdbcUrl = "jdbc:postgresql://" + rawUrl.substring("postgres://".length());
                    log.info("Conversion de l'URL DataSource (postgres:// -> jdbc:postgresql://)");
                    return jdbcUrl;
                }

                if (rawUrl.startsWith("postgresql://") && !rawUrl.startsWith("jdbc:postgresql://")) {
                    String jdbcUrl = "jdbc:postgresql://" + rawUrl.substring("postgresql://".length());
                    log.info("Conversion de l'URL DataSource (postgresql:// -> jdbc:postgresql://)");
                    return jdbcUrl;
                }

                return rawUrl;
            }
        };
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
