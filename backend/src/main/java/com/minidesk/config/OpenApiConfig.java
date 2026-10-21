package com.minidesk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    /** Nom du schéma de sécurité JWT réutilisé dans toute la config */
    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI miniDeskOpenAPI() {
        return new OpenAPI()

                // Informations générales de l'API
                .info(new Info()
                        .title(" MiniDesk API")
                        .description("""
                            API REST du système de gestion de tickets d'assistance MiniDesk.
                            
                            ## Authentification
                            Tous les endpoints (sauf `/api/auth/login`) nécessitent un token **JWT Bearer**.
                            
                            **Comment s'authentifier dans Swagger :**
                            1. Appelez `POST /api/auth/login` avec vos identifiants
                            2. Copiez le `token` retourné
                            3. Cliquez sur le bouton **Authorize 🔒** en haut à droite
                            4. Collez le token dans le champ `Value` puis cliquez Authorize
                            
                            ## Rôles disponibles
                            | Rôle | Accès |
                            |------|-------|
                            | `ADMIN` | Accès complet à toutes les routes |
                            | `TECHNICIAN` | Tickets + Stats dashboard |
                            | `USER` | Ses propres tickets + Chat IA |
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Khmaich Abdellah")
                                .email("abdellahkhmich.12@gmail.com")
                        )
                        .license(new License()
                                .name("Projet  MiniDesk")
                        )
                )

                // Serveurs déclarés (affiché dans le sélecteur Swagger)
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Serveur de développement local")
                ))

                // ── Configuration du schéma de sécurité JWT Bearer ───────────
                // Permet d'afficher le bouton "Authorize 🔒" dans Swagger UI
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .name(BEARER_SCHEME)
                                        .description("Collez ici le token JWT obtenu depuis POST /api/auth/login")
                        )
                )

                // Appliquer la sécurité JWT à tous les endpoints par défaut
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}