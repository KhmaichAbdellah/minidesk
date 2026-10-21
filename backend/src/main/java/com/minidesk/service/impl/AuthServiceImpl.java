package com.minidesk.service.impl;

import lombok.RequiredArgsConstructor;
import com.minidesk.dto.request.LoginRequest;
import com.minidesk.dto.response.AuthResponse;
import com.minidesk.entity.User;
import com.minidesk.exception.LoginException;
import com.minidesk.repository.UserRepository;
import com.minidesk.security.JwtService;
import com.minidesk.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implémentation du service d'authentification.
 *
 * Responsabilités :
 *   1. Vérifier que l'email existe en base de données.
 *   2. Vérifier que le mot de passe fourni correspond au hash stocké.
 *   3. Vérifier que le compte utilisateur est actif (enabled = true).
 *   4. Générer et retourner un token JWT signé.
 *
 * Les erreurs d'authentification lancent une {@link LoginException}
 * qui sera capturée par le {@link com.minidesk.exception.GlobalExceptionHandler}
 * et retournée au client comme HTTP 401 Unauthorized.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Repository pour chercher l'utilisateur par son email en base de données
    private final UserRepository userRepository;

    // Encodeur BCrypt : compare le mot de passe en clair avec le hash stocké
    private final PasswordEncoder passwordEncoder;

    // Service responsable de la génération et validation des tokens JWT
    private final JwtService jwtService;

    /**
     * Tente de connecter un utilisateur avec ses identifiants (email + mot de passe).
     *
     * Étapes de validation :
     *  1. L'email doit exister en base → sinon LoginException (401)
     *  2. Le mot de passe doit correspondre au hash → sinon LoginException (401)
     *  3. Le compte doit être actif (enabled=true) → sinon LoginException (401)
     *  4. Génération du JWT contenant email, rôle et ID utilisateur
     *
     * @param request DTO contenant { email, password }
     * @return AuthResponse contenant le token JWT
     * @throws LoginException si l'authentification échoue
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // ── Étape 1 : Vérifier l'existence de l'utilisateur ──────────────────
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new LoginException("Aucun compte trouvé avec cet email"));

        // ── Étape 2 : Vérifier le mot de passe ───────────────────────────────
        // passwordEncoder.matches() compare le mot de passe en clair avec le hash BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new LoginException("Mot de passe incorrect");
        }

        // ── Étape 3 : Vérifier que le compte est actif ───────────────────────
        // Un admin peut désactiver un compte via PATCH /api/admin/users/{id}/toggle
        if (!user.getEnabled()) {
            throw new LoginException("Ce compte est désactivé. Contactez l'administrateur.");
        }

        // ── Étape 4 : Générer le token JWT ───────────────────────────────────
        // Le token contiendra : sub (email), role, id (userId), expiration
        String token = jwtService.generateToken(user);

        // Retourner la réponse contenant le token JWT
        return new AuthResponse(token);
    }
}
