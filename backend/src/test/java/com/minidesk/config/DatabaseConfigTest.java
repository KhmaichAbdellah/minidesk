package com.minidesk.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConfigTest {

    @Test
    @DisplayName("Devrait extraire les identifiants et formater une URL JDBC valide sans '@' pour Render")
    void testParseRenderCloudUrl() {
        String input = "postgresql://minidesk_user:h2SMaqG2hQn0l9oNPzNfLPXl9vhv4TV4@dpg-dbl1unh5efls7380ie10-a/minideskdb";
        DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(input, null, null);

        assertEquals("jdbc:postgresql://dpg-dbl1unh5efls7380ie10-a:5432/minideskdb", info.jdbcUrl());
        assertEquals("minidesk_user", info.username());
        assertEquals("h2SMaqG2hQn0l9oNPzNfLPXl9vhv4TV4", info.password());
    }

    @Test
    @DisplayName("Devrait gérer une URL avec port explicite")
    void testParseUrlWithPort() {
        String input = "postgresql://myuser:mypass@db.host.com:5433/mydb";
        DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(input, null, null);

        assertEquals("jdbc:postgresql://db.host.com:5433/mydb", info.jdbcUrl());
        assertEquals("myuser", info.username());
        assertEquals("mypass", info.password());
    }

    @Test
    @DisplayName("Devrait conserver les URLs déjà préfixées par jdbc: sans '@'")
    void testNormalizeJdbcUrlAlreadyPrefixed() {
        String input = "jdbc:postgresql://localhost:5432/minideskdb";
        DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(input, "admin", "pass");

        assertEquals(input, info.jdbcUrl());
        assertEquals("admin", info.username());
        assertEquals("pass", info.password());
    }
}
