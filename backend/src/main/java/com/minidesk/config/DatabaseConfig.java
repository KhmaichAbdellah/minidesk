package com.minidesk.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Configuration de la DataSource pour supporter les formats d'URL cloud
 * (Render, Railway, Neon, Heroku, etc.).
 *
 * Convertit une URL de type 'postgresql://user:password@host/db' en URL JDBC valide
 * 'jdbc:postgresql://host:5432/db' en extrayant les identifiants embarqués.
 *
 * Utilise une injection @Value directe pour éviter tout conflit avec le
 * processus de validation d'URL de Spring Boot (DataSourceProperties.fromJdbcUrl).
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    public record ConnectionInfo(String jdbcUrl, String username, String password) {}

    /**
     * Convertit une URL brute (format cloud ou JDBC) en ConnectionInfo normalisée.
     * Supporte : postgresql://, postgres://, jdbc:postgresql://, jdbc:postgres://
     */
    public static ConnectionInfo parseConnectionInfo(String rawUrl, String fallbackUser, String fallbackPassword) {
        if (!StringUtils.hasText(rawUrl)) {
            return new ConnectionInfo(rawUrl, fallbackUser, fallbackPassword);
        }

        String trimmed = rawUrl.trim();

        // Format cloud avec identifiants embarqués : postgresql://user:pass@host/db
        if (trimmed.contains("@")) {
            try {
                String uriPart = trimmed;
                if (uriPart.startsWith("jdbc:postgresql://")) {
                    uriPart = uriPart.substring("jdbc:postgresql://".length());
                } else if (uriPart.startsWith("jdbc:postgres://")) {
                    uriPart = uriPart.substring("jdbc:postgres://".length());
                } else if (uriPart.startsWith("postgresql://")) {
                    uriPart = uriPart.substring("postgresql://".length());
                } else if (uriPart.startsWith("postgres://")) {
                    uriPart = uriPart.substring("postgres://".length());
                }

                URI uri = URI.create("http://" + uriPart);
                String userInfo = uri.getUserInfo();
                String host     = uri.getHost();
                int    port     = uri.getPort();
                String path     = uri.getPath();
                String query    = uri.getQuery();

                String username = fallbackUser;
                String password = fallbackPassword;

                if (StringUtils.hasText(userInfo)) {
                    if (userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        if (!StringUtils.hasText(username)) username = parts[0];
                        if (!StringUtils.hasText(password)) password = parts[1];
                    } else {
                        if (!StringUtils.hasText(username)) username = userInfo;
                    }
                }

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                jdbcUrl.append(host);
                jdbcUrl.append(":").append(port > 0 ? port : 5432);
                jdbcUrl.append(StringUtils.hasText(path) ? path : "/minideskdb");
                if (StringUtils.hasText(query)) {
                    jdbcUrl.append("?").append(query);
                }

                log.info("URL cloud convertie → JDBC : {}", jdbcUrl);
                return new ConnectionInfo(jdbcUrl.toString(), username, password);

            } catch (Exception e) {
                log.warn("Parsing URL cloud échoué, repli sur conversion simple : {}", e.getMessage());
            }
        }

        // Format JDBC classique ou sans '@'
        String jdbcUrl = trimmed;
        if (!jdbcUrl.startsWith("jdbc:")) {
            if (jdbcUrl.startsWith("postgres://")) {
                jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring("postgres://".length());
            } else if (jdbcUrl.startsWith("postgresql://")) {
                jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring("postgresql://".length());
            } else {
                jdbcUrl = "jdbc:postgresql://" + jdbcUrl;
            }
        }

        return new ConnectionInfo(jdbcUrl, fallbackUser, fallbackPassword);
    }

    /**
     * Crée la DataSource en injectant les propriétés brutes directement via @Value,
     * court-circuitant ainsi la validation d'URL de DataSourceProperties qui rejette
     * les URLs de type 'postgresql://' (sans préfixe 'jdbc:').
     */
    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/minideskdb}") String rawUrl,
            @Value("${spring.datasource.username:postgres}") String rawUsername,
            @Value("${spring.datasource.password:admin}")    String rawPassword) {

        ConnectionInfo info = parseConnectionInfo(rawUrl, rawUsername, rawPassword);

        log.info("Initialisation DataSource → url={}, user={}", info.jdbcUrl(), info.username());

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(info.jdbcUrl());
        if (StringUtils.hasText(info.username())) {
            ds.setUsername(info.username());
        }
        if (StringUtils.hasText(info.password())) {
            ds.setPassword(info.password());
        }
        return ds;
    }
}
