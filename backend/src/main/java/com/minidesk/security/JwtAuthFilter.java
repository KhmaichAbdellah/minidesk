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

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Pas de token → on laisse passer (Spring Security bloquera si endpoint protégé)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7); // Supprimer "Bearer "

        try {
            final String email = jwtService.extractEmail(token);
            final String role  = jwtService.extractRole(token);  // ex: "ADMIN", "USER", "TECH"

            // Si email extrait et pas encore authentifié dans le contexte
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null && jwtService.isTokenValid(token, user)) {

                    // Construire l'autorité Spring Security à partir du rôle
                    SimpleGrantedAuthority authority =
                            new SimpleGrantedAuthority("ROLE_" + role); // ROLE_ADMIN, ROLE_TECH...

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,       // principal = l'objet User complet
                                    null,       // credentials = null (déjà authentifié)
                                    List.of(authority)
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Enregistrer dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token invalide ou expiré → on ne bloque pas ici, Spring Security s'en charge
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
