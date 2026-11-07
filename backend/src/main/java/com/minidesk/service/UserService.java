package com.minidesk.service;

import com.minidesk.dto.request.UpdateProfileRequest;
import com.minidesk.dto.request.UserRequest;
import com.minidesk.dto.response.UserResponse;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Interface du service de gestion des utilisateurs et des profils.
 */
public interface UserService {
    /** Créer un nouvel utilisateur avec génération de mot de passe et envoi d'email. */
    UserResponse createUser(UserRequest request);

    /** Récupérer un utilisateur par son ID. */
    UserResponse getUserById(Long id);

    /** Récupérer la liste de tous les utilisateurs. */
    List<UserResponse> getAllUsers();

    /** Mettre à jour les informations d'un utilisateur (nom, email, rôle). */
    UserResponse updateUser(Long id, UserRequest request);

    /** Supprimer un utilisateur par son ID. */
    void deleteUser(Long id);

    /** Activer ou désactiver le compte d'un utilisateur (enabled = true/false). */
    void toggleUserStatus(Long id);

    /** Mettre à jour le profil de l'utilisateur connecté. */
    UserResponse updateProfile(Long id, @Valid UpdateProfileRequest request);

    /** Changer le mot de passe de l'utilisateur connecté. */
    void changePassword(Long id, String currentPassword, String newPassword);

    /** Réinitialiser le mot de passe d'un utilisateur et retourner le nouveau mot de passe généré. */
    String resetPassword(Long id);
}
