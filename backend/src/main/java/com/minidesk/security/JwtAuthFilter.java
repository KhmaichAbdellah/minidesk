package com.minidesk.security;

import com.minidesk.entity.User;
import com.minidesk.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtre de sécurité exécuté UNE FOIS par requête HTTP (OncePerRequestFilter).
 *
 * Rôle :
 *   1. Intercepte le header HTTP "Authorization: Bearer <token>".
 *   2. Extrait et valide le token JWT via {@link JwtService}.
 *   3. Charge l'utilisateur depuis la base de données et construit son autorité ("ROLE_ADMIN", "ROLE_USER", etc.).
 *   4. Remplit le contexte de sécurité de Spring Security (SecurityContextHolder).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    // Service pour extraire et valider les tokens JWT
    private final JwtService jwtService;

    // Repository pour charger les données à jour de l'utilisateur
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Récupérer le header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Si pas de header ou s'il ne commence pas par "Bearer ", on laisse passer au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrait la chaîne du token en supprimant le préfixe "Bearer " (7 caractères)
        final String token = authHeader.substring(7);

        try {
            // Extrait l'email (subject) et le rôle du token JWT
            final String email = jwtService.extractEmail(token);
            final String role  = jwtService.extractRole(token);  // "ADMIN", "USER", "TECHNICIAN"

            // Si l'email est valide et qu'aucune authentification n'est encore définie dans le contexte courant
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Charger l'utilisateur en base de données
                User user = userRepository.findByEmail(email).orElse(null);

                // Vérifier que l'utilisateur existe, que son compte est actif et que le token est valide
                if (user != null && user.getEnabled() && jwtService.isTokenValid(token, user)) {

                    // Construire l'autorité Spring Security au format "ROLE_<ROLE>" (ex: ROLE_ADMIN, ROLE_USER)
                    SimpleGrantedAuthority authority =
                            new SimpleGrantedAuthority("ROLE_" + role);

                    // Créer l'objet d'authentification pour Spring Security avec l'objet User complet comme Principal
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,       // principal = l'entité User complète
                                    null,       // credentials = null (déjà authentifié)
                                    List.of(authority) // liste des autorités/rôles
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Enregistrer l'authentification dans le contexte de sécurité de l'application
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // En cas d'erreur de décodage ou token expiré, nettoyer le contexte de sécurité
            SecurityContextHolder.clearContext();
        }

        // Continuer l'exécution de la chaîne de filtres Spring Security
        filterChain.doFilter(request, response);
    }
}
