package com.minidesk.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConfigTest {

    @Test
    @DisplayName("Devrait convertir postgresql:// en jdbc:postgresql://")
    void testNormalizePostgresqlUrl() {
        String input = "postgresql://user:pass@dpg-xxx.render.com:5432/minideskdb";
        String expected = "jdbc:postgresql://user:pass@dpg-xxx.render.com:5432/minideskdb";
        assertEquals(expected, DatabaseConfig.normalizeJdbcUrl(input));
    }

    @Test
    @DisplayName("Devrait convertir postgres:// en jdbc:postgresql://")
    void testNormalizePostgresUrl() {
        String input = "postgres://user:pass@dpg-xxx.render.com:5432/minideskdb";
        String expected = "jdbc:postgresql://user:pass@dpg-xxx.render.com:5432/minideskdb";
        assertEquals(expected, DatabaseConfig.normalizeJdbcUrl(input));
    }

    @Test
    @DisplayName("Devrait conserver les URLs déjà préfixées par jdbc:")
    void testNormalizeJdbcUrlAlreadyPrefixed() {
        String input = "jdbc:postgresql://localhost:5432/minideskdb";
        assertEquals(input, DatabaseConfig.normalizeJdbcUrl(input));
    }
}
