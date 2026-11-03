package com.minidesk.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration principale de la sécurité de l'application via Spring Security.
 *
 * Concepts configurés :
 *   1. Statelesse (sans session HTTP) : chaque requête doit porter un token JWT
 *   2. CORS (Cross-Origin Resource Sharing) : autorise les requêtes frontend React (port 5173/3000)
 *   3. CSRF désactivé : inutile pour une API REST stateless avec JWT
 *   4. Règles d'accès HTTP par route et rôle
 *   5. Filtre d'authentification JWT personnalisé (JwtAuthFilter)
 */
@Configuration                        // Indique que cette classe contient des définitions de @Bean Spring
@EnableWebSecurity                    // Active les fonctionnalités de sécurité Web de Spring Security
@EnableMethodSecurity                // Active les annotations de sécurité au niveau des méthodes (@PreAuthorize)
@RequiredArgsConstructor              // Génère le constructeur pour l'injection du JwtAuthFilter
public class SecurityConfig {

    // Filtre JWT personnalisé qui vérifie le token Bearer de chaque requête HTTP
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Définit la chaîne de filtres de sécurité HTTP (SecurityFilterChain).
     *
     * @param http L'objet HttpSecurity permettant de configurer les filtres
     * @return La chaîne de filtres configurée
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── Active la configuration CORS définie dans corsConfigurationSource() ──
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // ── Désactive la protection CSRF (inutile pour API stateless JWT) ──
                .csrf(csrf -> csrf.disable())
                
                // ── Politique de gestion de session : STATELESS (aucune session côté serveur) ──
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // ── Configuration des autorisations d'accès aux routes ──
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics d'authentification (/api/auth/login)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**", "/api/health/**", "/health").permitAll()

                        // Swagger / OpenApi documentation endpoints publics
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Endpoints réservés aux Administrateurs
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Endpoints réservés aux Techniciens et Administrateurs
                        .requestMatchers("/api/tech/**").hasAnyRole("TECHNICIAN", "ADMIN")

                        // Toute autre requête nécessite un utilisateur authentifié
                        .anyRequest().authenticated()
                )
                // ── Insère notre Filtre JWT AVANT le filtre d'authentification standard par mot de passe ──
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration des règles CORS (Cross-Origin Resource Sharing).
     * Permet au frontend React de communiquer avec le backend Spring Boot.
     *
     * @return CorsConfigurationSource configuré pour autoriser le frontend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Autorise toutes les origines en mode développement (ex: http://localhost:5173)
        configuration.setAllowedOriginPatterns(List.of("*"));
        // Autorise les méthodes HTTP standards
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Autorise tous les headers (Authorization, Content-Type, etc.)
        configuration.setAllowedHeaders(List.of("*"));
        // Permet l'envoi de credentials/cookies si nécessaire
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Encodeur de mot de passe utilisant l'algorithme fort BCrypt.
     * Utilisé pour hacher les mots de passe avant stockage en base de données.
     *
     * @return Instance de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
