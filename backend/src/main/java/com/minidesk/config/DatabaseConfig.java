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
import java.net.URI;

/**
 * Configuration automatique de la DataSource pour supporter les formats d'URL
 * de bases de données cloud (Render, Railway, Neon, Heroku, etc.).
 *
 * Extrait proprement les identifiants si l'URL est sous forme URI standard
 * 'postgresql://user:password@host[:port]/database' pour générer une URL JDBC valide
 * 'jdbc:postgresql://host:port/database' sans perturber le driver PostgreSQL.
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    public record ConnectionInfo(String jdbcUrl, String username, String password) {}

    public static ConnectionInfo parseConnectionInfo(String rawUrl, String fallbackUser, String fallbackPassword) {
        if (!StringUtils.hasText(rawUrl)) {
            return new ConnectionInfo(rawUrl, fallbackUser, fallbackPassword);
        }

        String trimmed = rawUrl.trim();

        // Format cloud contenant des identifiants (ex: postgresql://user:password@host/db)
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
                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath();
                String query = uri.getQuery();

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
                if (port > 0) {
                    jdbcUrl.append(":").append(port);
                } else {
                    jdbcUrl.append(":5432");
                }
                if (StringUtils.hasText(path)) {
                    jdbcUrl.append(path);
                } else {
                    jdbcUrl.append("/minideskdb");
                }
                if (StringUtils.hasText(query)) {
                    jdbcUrl.append("?").append(query);
                }

                log.info("URL cloud détectée et convertie en JDBC standard : {}", jdbcUrl);
                return new ConnectionInfo(jdbcUrl.toString(), username, password);

            } catch (Exception e) {
                log.warn("Erreur lors du parsing de l'URL cloud avec @, repli sur méthode simple : {}", e.getMessage());
            }
        }

        // Format JDBC classique sans '@'
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

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties() {
            @Override
            public void setUrl(String url) {
                ConnectionInfo info = parseConnectionInfo(url, getUsername(), getPassword());
                super.setUrl(info.jdbcUrl());
                if (StringUtils.hasText(info.username())) {
                    super.setUsername(info.username());
                }
                if (StringUtils.hasText(info.password())) {
                    super.setPassword(info.password());
                }
            }

            @Override
            public String determineUrl() {
                ConnectionInfo info = parseConnectionInfo(super.determineUrl(), getUsername(), getPassword());
                return info.jdbcUrl();
            }

            @Override
            public String determineUsername() {
                ConnectionInfo info = parseConnectionInfo(super.determineUrl(), super.determineUsername(), getPassword());
                return info.username();
            }

            @Override
            public String determinePassword() {
                ConnectionInfo info = parseConnectionInfo(super.determineUrl(), getUsername(), super.determinePassword());
                return info.password();
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
        ConnectionInfo info = parseConnectionInfo(properties.determineUrl(), properties.determineUsername(), properties.determinePassword());

        log.info("Initialisation de la DataSource avec URL JDBC: {}, user: {}", info.jdbcUrl(), info.username());

        return DataSourceBuilder.create()
                .driverClassName(properties.determineDriverClassName())
                .url(info.jdbcUrl())
                .username(info.username())
                .password(info.password())
                .build();
    }
}
