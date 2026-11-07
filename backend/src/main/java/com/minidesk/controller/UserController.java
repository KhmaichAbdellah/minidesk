package com.minidesk.controller;

import com.minidesk.dto.request.UserRequest;
import com.minidesk.dto.response.UserResponse;
import com.minidesk.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST de gestion des utilisateurs.
 * Base URL : /api/admin/users
 * Accès : ADMIN uniquement (contrôlé par @PreAuthorize au niveau classe)
 */
@Tag(name = "👥 Utilisateurs (Admin)", description = "Gestion des comptes utilisateurs — ADMIN uniquement")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    // ── POST /api/admin/users ─────────────────────────────────────────────────
    @Operation(summary = "Créer un utilisateur",
        description = "Crée un compte utilisateur. Un mot de passe est généré automatiquement et envoyé par email.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utilisateur créé, email envoyé"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé")
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/admin/users ──────────────────────────────────────────────────
    @Operation(summary = "Lister tous les utilisateurs", description = "Retourne la liste complète de tous les comptes enregistrés.")
    @ApiResponse(responseCode = "200", description = "Liste retournée")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ── GET /api/admin/users/{id} ─────────────────────────────────────────────
    @Operation(summary = "Obtenir un utilisateur par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID de l'utilisateur", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ── PUT /api/admin/users/{id} ─────────────────────────────────────────────
    @Operation(summary = "Modifier un utilisateur", description = "Met à jour le nom, l'email et le rôle d'un utilisateur.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID de l'utilisateur", example = "1") @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // ── DELETE /api/admin/users/{id} ──────────────────────────────────────────
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime définitivement le compte utilisateur et toutes ses données associées.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Utilisateur supprimé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID de l'utilisateur à supprimer", example = "1") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ── PATCH /api/admin/users/{id}/toggle ───────────────────────────────────
    @Operation(summary = "Activer / Désactiver un compte",
        description = "Bascule l'état `enabled` du compte (true ↔ false). Un compte désactivé ne peut plus se connecter.")
    @ApiResponse(responseCode = "200", description = "Statut du compte modifié")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<String> toggleUserStatus(
            @Parameter(description = "ID de l'utilisateur", example = "1") @PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok("Statut du compte mis à jour avec succès");
    }

    // ── POST /api/admin/users/{id}/reset-password ───────────────────────────
    @Operation(summary = "Réinitialiser le mot de passe d'un utilisateur",
        description = "Génère un nouveau mot de passe aléatoire, l'envoie par email et le retourne dans la réponse.")
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(
            @Parameter(description = "ID de l'utilisateur", example = "1") @PathVariable Long id) {
        String newPassword = userService.resetPassword(id);
        return ResponseEntity.ok(java.util.Map.of("temporaryPassword", newPassword));
    }
}