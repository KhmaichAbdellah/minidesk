package com.minidesk.controller;

import com.minidesk.dto.request.UserRequest;
import com.minidesk.dto.response.UserResponse;
import com.minidesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // Tous les endpoints de ce controller → ADMIN seulement
public class UserController {

    private final UserService userService;

    // ── POST /api/admin/users ─────────────────────────────────────────────────
    // Créer un utilisateur → mdp généré auto + envoi email
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/admin/users ──────────────────────────────────────────────────
    // Lister tous les utilisateurs
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ── GET /api/admin/users/{id} ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ── PUT /api/admin/users/{id} ─────────────────────────────────────────────
    // Modifier nom, email, rôle
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // ── DELETE /api/admin/users/{id} ──────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ── PATCH /api/admin/users/{id}/toggle ───────────────────────────────────
    // Activer ou désactiver un compte
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok("Statut du compte mis à jour avec succès");
    }
}