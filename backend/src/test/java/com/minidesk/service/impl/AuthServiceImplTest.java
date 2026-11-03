package com.minidesk.service.impl;

import com.minidesk.dto.request.LoginRequest;
import com.minidesk.dto.response.AuthResponse;
import com.minidesk.entity.User;
import com.minidesk.enums.Role;
import com.minidesk.exception.LoginException;
import com.minidesk.repository.UserRepository;
import com.minidesk.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link AuthServiceImpl}.
 * 
 * Vérifie les 3 cas d'échec (email inexistant, mauvais mot de passe, compte désactivé)
 * et le cas de succès (retour d'un token JWT).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    // ── Mocks des dépendances injectées ────────────────────────────────────────
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    // Classe testée (les @Mock sont injectés automatiquement)
    @InjectMocks
    private AuthServiceImpl authService;

    // Utilisateur de test réutilisé dans plusieurs tests
    private User testUser;

    @BeforeEach
    void setUp() {
        // Crée un utilisateur actif avec mot de passe haché simulé
        testUser = User.builder()
                .id(1L)
                .name("Abdellah Dev")
                .email("admin@minidesk.com")
                .password("$2a$10$hashedPassword")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
    }

    // ── Test 1 : Email introuvable en base de données ─────────────────────────
    @Test
    @DisplayName("login : email inconnu → lance LoginException")
    void login_emailInconnu_lancerException() {
        // Simuler : aucun utilisateur trouvé avec cet email
        when(userRepository.findByEmail("inconnu@test.com"))
                .thenReturn(Optional.empty());

        // Utiliser les setters Lombok (@Data génère setters, pas de constructeur avec args)
        LoginRequest req = new LoginRequest();
        req.setEmail("inconnu@test.com");
        req.setPassword("motdepasse");

        // Vérifier que LoginException est bien lancée
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("Aucun compte");
    }

    // ── Test 2 : Mot de passe incorrect ───────────────────────────────────────
    @Test
    @DisplayName("login : mot de passe incorrect → lance LoginException")
    void login_motDePasseIncorrect_lancerException() {
        when(userRepository.findByEmail("admin@minidesk.com"))
                .thenReturn(Optional.of(testUser));

        // Simuler : le mot de passe ne correspond pas au hash BCrypt
        when(passwordEncoder.matches("mauvaisMotDePasse", testUser.getPassword()))
                .thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setEmail("admin@minidesk.com");
        req.setPassword("mauvaisMotDePasse");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("Mot de passe incorrect");
    }

    // ── Test 3 : Compte désactivé ─────────────────────────────────────────────
    @Test
    @DisplayName("login : compte désactivé → lance LoginException")
    void login_compteDesactive_lancerException() {
        // Désactiver le compte
        testUser.setEnabled(false);

        when(userRepository.findByEmail("admin@minidesk.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setEmail("admin@minidesk.com");
        req.setPassword("monMotDePasse");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("désactivé");
    }

    // ── Test 4 : Authentification réussie ─────────────────────────────────────
    @Test
    @DisplayName("login : identifiants valides → retourne AuthResponse avec JWT")
    void login_identifiantsValides_retournerToken() {
        when(userRepository.findByEmail("admin@minidesk.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("monMotDePasse", testUser.getPassword()))
                .thenReturn(true);
        // Simuler la génération d'un token JWT
        when(jwtService.generateToken(testUser))
                .thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature");

        LoginRequest req = new LoginRequest();
        req.setEmail("admin@minidesk.com");
        req.setPassword("monMotDePasse");
        AuthResponse response = authService.login(req);

        // Vérifications
        assertThat(response).isNotNull();
        assertThat(response.getToken()).startsWith("eyJ");
        verify(jwtService, times(1)).generateToken(testUser);
    }
}
