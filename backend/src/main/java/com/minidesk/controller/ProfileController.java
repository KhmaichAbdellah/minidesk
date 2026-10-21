package com.minidesk.controller;

import com.minidesk.dto.request.ChangePasswordRequest;
import com.minidesk.dto.request.UpdateProfileRequest;
import com.minidesk.dto.response.UserResponse;
import com.minidesk.entity.User;
import com.minidesk.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion du profil utilisateur.
 * Base URL : /api/profile
 *
 * Tous les endpoints permettent à l'utilisateur connecté
 * de consulter ou modifier uniquement ses propres données personnelles.
 */
@Tag(name = "👤 Profil", description = "Consultation et modification du profil de l'utilisateur connecté")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;


    @Operation(summary = "Voir mon profil", description = "Retourne les informations du profil de l'utilisateur actuellement connecté.")
    @ApiResponse(responseCode = "200", description = "Profil retourné")
    @GetMapping
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getUserById(currentUser.getId()));
    }


    @Operation(summary = "Modifier mon profil", description = "Permet à l'utilisateur connecté de modifier son nom et son email.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil mis à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse updated = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updated);
    }


    @Operation(summary = "Changer mon mot de passe",
        description = "Permet à l'utilisateur connecté de changer son mot de passe en fournissant l'ancien et le nouveau.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mot de passe modifié avec succès"),
        @ApiResponse(responseCode = "400", description = "Ancien mot de passe incorrect ou nouveau mot de passe invalide")
    })
    @PutMapping("/password")
    public ResponseEntity<String> changeMyPassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Mot de passe modifié avec succès");
    }
}
