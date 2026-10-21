package com.minidesk.service.impl;

import com.minidesk.dto.request.UpdateProfileRequest;
import com.minidesk.dto.request.UserRequest;
import com.minidesk.dto.response.UserResponse;
import com.minidesk.entity.User;
import com.minidesk.exception.ResourceNotFoundException;
import com.minidesk.repository.UserRepository;
import com.minidesk.service.EmailService;
import com.minidesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des utilisateurs.
 *
 * Responsabilités :
 *   - Création de comptes avec génération automatique de mot de passe
 *   - Envoi d'email de bienvenue avec les identifiants
 *   - CRUD complet sur les utilisateurs (admin uniquement)
 *   - Activation / désactivation de comptes
 *   - Mise à jour du profil et changement de mot de passe
 *
 * Accès : la plupart des opérations sont réservées aux ADMIN
 * (contrôle d'accès défini au niveau du contrôleur avec @PreAuthorize).
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // Repository JPA pour toutes les opérations CRUD sur les utilisateurs
    private final UserRepository userRepository;

    // Encodeur BCrypt pour hacher les mots de passe avant stockage en base
    private final PasswordEncoder passwordEncoder;

    // Service d'envoi d'email pour notifier les nouveaux utilisateurs
    private final EmailService emailService;

    // ── Constantes pour la génération de mots de passe ───────────────────────
    // Caractères autorisés dans les mots de passe générés automatiquement
    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";

    // Longueur du mot de passe généré automatiquement
    private static final int PASSWORD_LENGTH = 10;

    // ────────────────────────────────────────────────────────────────────────
    // UTILITAIRES PRIVÉS
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Génère un mot de passe aléatoire sécurisé de longueur {@value #PASSWORD_LENGTH}.
     *
     * Utilise {@link SecureRandom} (cryptographiquement sécurisé) plutôt que
     * {@link java.util.Random} (non sécurisé pour les mots de passe).
     *
     * @return Mot de passe aléatoire de 10 caractères
     */
    private String generatePassword() {
        SecureRandom random = new SecureRandom();  // Générateur aléatoire cryptographiquement sûr
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            // Choisir un caractère aléatoire parmi la liste CHARS
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Convertit une entité JPA {@link User} en DTO {@link UserResponse}.
     *
     * Le DTO ne contient jamais le mot de passe hashé pour des raisons de sécurité.
     *
     * @param user L'entité utilisateur chargée depuis la base
     * @return DTO UserResponse sérialisable en JSON
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())               // ID unique en base de données
                .name(user.getName())           // Nom complet de l'utilisateur
                .email(user.getEmail())         // Email (identifiant de connexion)
                .role(user.getRole())           // Rôle : ADMIN, TECHNICIAN ou USER
                .enabled(user.getEnabled())     // true = compte actif, false = compte désactivé
                .createdAt(user.getCreatedAt()) // Date de création du compte
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // CRÉER UN UTILISATEUR (Admin seulement)
    // POST /api/admin/users
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Crée un nouveau compte utilisateur avec un mot de passe généré automatiquement.
     *
     * Flux complet :
     *   1. Vérifier que l'email n'est pas déjà utilisé
     *   2. Générer un mot de passe aléatoire sécurisé
     *   3. Hasher le mot de passe avec BCrypt
     *   4. Persister l'utilisateur en base de données
     *   5. Envoyer un email avec le mot de passe en clair (avant hashage)
     *
     * @param request DTO contenant { name, email, role }
     * @return DTO UserResponse de l'utilisateur créé
     * @throws RuntimeException si l'email existe déjà (→ HTTP 409 via GlobalExceptionHandler)
     */
    @Override
    public UserResponse createUser(UserRequest request) {

        // ── Vérifier l'unicité de l'email ─────────────────────────────────────
        // La table users a une contrainte UNIQUE sur la colonne email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // ── Générer le mot de passe en clair (avant hashage) ──────────────────
        // Ce mot de passe sera envoyé par email et JAMAIS stocké en clair
        String rawPassword = generatePassword();

        // ── Construire et persister l'entité utilisateur ──────────────────────
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))  // Hash BCrypt du mot de passe
                .role(request.getRole())
                .enabled(true)  // Compte actif par défaut
                .build();

        User saved = userRepository.save(user);  // INSERT SQL

        // ── Envoyer l'email de bienvenue avec le mot de passe en clair ─────────
        // IMPORTANT : envoyer AVANT de perdre la référence au mot de passe en clair
        emailService.sendEmail(saved.getEmail(), saved.getName(), rawPassword);

        return toResponse(saved);
    }

    // ────────────────────────────────────────────────────────────────────────
    // RÉCUPÉRER UN UTILISATEUR PAR ID
    // GET /api/admin/users/{id}
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Retourne un utilisateur spécifique par son ID.
     *
     * @param id L'ID de l'utilisateur à récupérer
     * @return DTO UserResponse
     * @throws ResourceNotFoundException si l'ID n'existe pas en base (→ HTTP 404)
     */
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));
        return toResponse(user);
    }

    // ────────────────────────────────────────────────────────────────────────
    // RÉCUPÉRER TOUS LES UTILISATEURS
    // GET /api/admin/users
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Retourne la liste de tous les utilisateurs en base de données.
     *
     * @return Liste de DTO UserResponse (peut être vide mais jamais null)
     */
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)         // Convertir chaque entité en DTO
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────────────────
    // MODIFIER UN UTILISATEUR (nom, email, rôle)
    // PUT /api/admin/users/{id}
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Met à jour le nom, l'email et le rôle d'un utilisateur existant.
     *
     * @param id      L'ID de l'utilisateur à modifier
     * @param request DTO contenant { name, email, role }
     * @return DTO UserResponse mis à jour
     * @throws ResourceNotFoundException si l'ID n'existe pas (→ HTTP 404)
     */
    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));

        // Mettre à jour les champs modifiables par l'admin
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        return toResponse(userRepository.save(user));  // UPDATE SQL
    }

    // ────────────────────────────────────────────────────────────────────────
    // SUPPRIMER UN UTILISATEUR
    // DELETE /api/admin/users/{id}
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Supprime définitivement un utilisateur de la base de données.
     *
     * ⚠️  Attention : la suppression est en cascade sur les tickets créés par cet utilisateur
     * (CascadeType.ALL défini dans User.createdTickets).
     *
     * @param id L'ID de l'utilisateur à supprimer
     * @throws ResourceNotFoundException si l'ID n'existe pas (→ HTTP 404)
     */
    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id);
        }
        userRepository.deleteById(id);  // DELETE SQL (avec cascade)
    }

    // ────────────────────────────────────────────────────────────────────────
    // ACTIVER / DÉSACTIVER UN COMPTE
    // PATCH /api/admin/users/{id}/toggle
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Bascule l'état d'activation d'un compte utilisateur (true ↔ false).
     *
     * Un compte désactivé (enabled=false) ne peut plus se connecter
     * (vérifié dans AuthServiceImpl lors du login).
     *
     * @param id L'ID du compte à activer/désactiver
     * @throws ResourceNotFoundException si l'ID n'existe pas (→ HTTP 404)
     */
    @Override
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));

        // Inverser l'état : true → false (désactivation) ou false → true (activation)
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);  // UPDATE SQL : SET enabled = ?
    }

    // ────────────────────────────────────────────────────────────────────────
    // MISE À JOUR DU PROFIL (par l'utilisateur lui-même)
    // PUT /api/profile
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Permet à un utilisateur de mettre à jour son propre profil (nom et email).
     *
     * Vérifie que le nouvel email n'est pas déjà utilisé par un autre compte.
     *
     * @param id      L'ID de l'utilisateur (extrait du JWT par le contrôleur)
     * @param request DTO contenant { name, email }
     * @return DTO UserResponse mis à jour
     * @throws RuntimeException          si le nouvel email est déjà pris (→ HTTP 409)
     * @throws ResourceNotFoundException si l'ID n'existe pas (→ HTTP 404)
     */
    @Override
    public UserResponse updateProfile(Long id, @Valid UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Vérifier que le nouvel email n'est pas déjà utilisé par UN AUTRE utilisateur
        // (on accepte de garder le même email → pas de conflit)
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre compte");
        }

        // Mettre à jour le nom et l'email
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        return toResponse(userRepository.save(user));  // UPDATE SQL
    }

    // ────────────────────────────────────────────────────────────────────────
    // CHANGEMENT DE MOT DE PASSE (par l'utilisateur lui-même)
    // PUT /api/profile/password
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Permet à un utilisateur de changer son propre mot de passe.
     *
     * Sécurité : le mot de passe actuel doit être fourni pour confirmer l'identité.
     * Le nouveau mot de passe est hashé avec BCrypt avant stockage.
     *
     * @param id              L'ID de l'utilisateur
     * @param currentPassword Le mot de passe actuel en clair (pour vérification)
     * @param newPassword     Le nouveau mot de passe en clair (sera hashé)
     * @throws IllegalArgumentException  si le mot de passe actuel est incorrect (→ HTTP 400)
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas (→ HTTP 404)
     */
    @Override
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Vérifier que le mot de passe actuel fourni correspond au hash stocké en base
        // passwordEncoder.matches(clair, hash) → true si correspondance
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect");
        }

        // Hasher et sauvegarder le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));  // Hash BCrypt
        userRepository.save(user);  // UPDATE SQL : SET password = ?
    }
}
