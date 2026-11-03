package com.minidesk.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * Configuration automatique de la DataSource pour supporter les formats d'URL
 * de bases de données cloud (Render, Railway, Neon, Heroku, etc.).
 *
 * Normalise automatiquement 'postgres://' et 'postgresql://' en 'jdbc:postgresql://'
 * dès la liaison des propriétés pour éviter l'exception 'url must start with jdbc'.
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    public static String normalizeJdbcUrl(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return rawUrl;
        }

        String trimmed = rawUrl.trim();

        if (trimmed.startsWith("jdbc:")) {
            return trimmed;
        }

        if (trimmed.startsWith("postgres://")) {
            String converted = "jdbc:postgresql://" + trimmed.substring("postgres://".length());
            log.info("Conversion URL DataSource (postgres:// -> jdbc:postgresql://)");
            return converted;
        }

        if (trimmed.startsWith("postgresql://")) {
            String converted = "jdbc:postgresql://" + trimmed.substring("postgresql://".length());
            log.info("Conversion URL DataSource (postgresql:// -> jdbc:postgresql://)");
            return converted;
        }

        return "jdbc:postgresql://" + trimmed;
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties() {
            @Override
            public void setUrl(String url) {
                super.setUrl(normalizeJdbcUrl(url));
            }

            @Override
            public String determineUrl() {
                return normalizeJdbcUrl(super.determineUrl());
            }

            @Override
            public String determineDriverClassName() {
                String existing = super.getDriverClassName();
                if (StringUtils.hasText(existing)) {
                    return existing;
                }
                return "org.postgresql.Driver";
            }
        };
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String finalUrl = normalizeJdbcUrl(properties.determineUrl());
        log.info("Initialisation de la DataSource avec URL: {}", finalUrl);

        return DataSourceBuilder.create()
                .driverClassName(properties.determineDriverClassName())
                .url(finalUrl)
                .username(properties.determineUsername())
                .password(properties.determinePassword())
                .build();
    }
}
