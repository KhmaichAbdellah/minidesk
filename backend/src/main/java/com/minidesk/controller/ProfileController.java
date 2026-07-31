package com.minidesk.controller;

import com.minidesk.dto.request.UpdateProfileRequest;
import com.minidesk.dto.response.UserResponse;
import com.minidesk.entity.User;
import com.minidesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    // ── GET /api/profile ──────────────────────────────────────────────────────
    // Voir son propre profil
    @GetMapping
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getUserById(currentUser.getId()));
    }

    // ── PUT /api/profile ──────────────────────────────────────────────────────
    // Modifier son nom et email (chaque user modifie UNIQUEMENT son propre profil)
    @PutMapping
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {

        // currentUser.getId() → l'ID extrait du JWT, impossible à falsifier
        UserResponse updated = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updated);
    }
}
