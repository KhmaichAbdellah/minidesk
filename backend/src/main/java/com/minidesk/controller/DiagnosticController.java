package com.minidesk.controller;

import com.minidesk.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/admin/diagnostic")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class DiagnosticController {

    private final EmailService emailService;

    /**
     * Envoie un email de test à l'adresse indiquée pour valider la configuration SMTP.
     *
     * Exemple d'appel :
     *   POST /api/admin/diagnostic/test-email
     *   Body: { "to": "votre@email.com" }
     *
     * @param body Map contenant la clé "to" avec l'adresse destinataire
     * @return 200 OK si l'email est envoyé, 500 si erreur SMTP
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, String>> testEmail(@RequestBody Map<String, String> body) {
        String to = body.get("to");

        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERREUR",
                    "message", "Le champ 'to' (adresse email destinataire) est obligatoire."
            ));
        }

        log.info("=== TEST EMAIL SMTP : envoi vers {} ===", to);

        try {
            // Envoyer un email de test en utilisant le template de bienvenue existant
            emailService.sendEmail(to, "Test SMTP MiniDesk", "TEST-SMTP-OK");

            log.info("Email de test envoyé avec succès vers {}", to);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCÈS",
                    "message", "Email de test envoyé avec succès à : " + to,
                    "destinataire", to
            ));

        } catch (Exception e) {
            log.error("Échec de l'envoi de l'email de test vers {} : {}", to, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "ÉCHEC",
                    "message", "Impossible d'envoyer l'email. Erreur : " + e.getMessage(),
                    "conseil", "Vérifiez la configuration SMTP dans application.yaml (host, port, username, password)."
            ));
        }
    }
}
