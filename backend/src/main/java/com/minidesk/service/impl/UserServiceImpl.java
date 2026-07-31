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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ── Caractères pour le mot de passe généré ────────────────────────────────
    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";
    private static final int PASSWORD_LENGTH = 10;

    // ── Générer un mot de passe aléatoire sécurisé ───────────────────────────
    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    // ── Mapper User → UserResponse ────────────────────────────────────────────
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRÉER UN UTILISATEUR (Admin seulement)
    // 1. Générer mdp aléatoire
    // 2. Hasher et sauvegarder
    // 3. Envoyer email avec mdp en clair
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public UserResponse createUser(UserRequest request) {

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Générer le mot de passe en clair
        String rawPassword = generatePassword();

        // Construire et sauvegarder l'utilisateur
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))  // hashé en base
                .role(request.getRole())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);

        // Envoyer l'email avec le mot de passe en clair
        emailService.sendEmail(saved.getEmail(), saved.getName(), rawPassword);

        return toResponse(saved);
    }

    // ── Récupérer un utilisateur par ID ──────────────────────────────────────
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));
        return toResponse(user);
    }

    // ── Récupérer tous les utilisateurs ──────────────────────────────────────
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Modifier un utilisateur (nom, email, rôle) ────────────────────────────
    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        return toResponse(userRepository.save(user));
    }

    // ── Supprimer un utilisateur ──────────────────────────────────────────────
    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id);
        }
        userRepository.deleteById(id);
    }

    // ── Activer / Désactiver un compte ────────────────────────────────────────
    @Override
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));

        user.setEnabled(!user.getEnabled());  // bascule true ↔ false
        userRepository.save(user);
    }


    @Override
    public UserResponse updateProfile(Long id, @Valid UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Vérifier si le nouvel email est déjà pris par un autre user
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        return toResponse(userRepository.save(user));
    }
}
