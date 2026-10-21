package com.minidesk.controller;

import com.minidesk.dto.request.LoginRequest;
import com.minidesk.dto.response.AuthResponse;
import com.minidesk.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "🔐 Authentification", description = "Endpoint public de connexion — retourne un token JWT")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @Operation(
        summary = "Connexion utilisateur",
        description = "Authentifie un utilisateur avec son email et mot de passe. Retourne un token JWT à utiliser dans le header `Authorization: Bearer <token>`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Connexion réussie — token JWT retourné",
            content = @Content(schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiJ9...\"}"))),
        @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect, ou compte désactivé",
            content = @Content(examples = @ExampleObject(value = "{\"message\": \"Mot de passe incorrect\"}")))
    })
    // @SecurityRequirements(value = {}) — Cet endpoint est PUBLIC : pas besoin de JWT
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Identifiants de connexion",
                content = @Content(examples = @ExampleObject(
                    value = "{\"email\": \"admin@minidesk.com\", \"password\": \"Admin@2026!\"}"))
            )
            @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
