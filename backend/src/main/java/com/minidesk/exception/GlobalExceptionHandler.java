package com.minidesk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions REST.
 *
 * Annoté avec @RestControllerAdvice : intercepte les exceptions lancées par
 * n'importe quel contrôleur REST (@RestController) et les convertit en
 * réponses HTTP appropriées avec le bon code de statut.
 *
 * Hiérarchie des handlers (du plus spécifique au plus général) :
 *   LoginException           → 401 Unauthorized
 *   ResourceNotFoundException → 404 Not Found
 *   AccessDeniedException    → 403 Forbidden
 *   IllegalArgumentException → 400 Bad Request
 *   MethodArgumentNotValidException → 400 Bad Request (avec détails par champ)
 *   RuntimeException         → 409 Conflict (ex : email déjà existant)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs d'authentification (mauvais email ou mot de passe).
     * Retourne HTTP 401 Unauthorized.
     *
     * @param ex L'exception contenant le message d'erreur
     * @return ResponseEntity avec le message d'erreur et statut 401
     */
    @ExceptionHandler(LoginException.class)
    public ResponseEntity<String> handleLoginException(LoginException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)  // 401 : identifiants incorrects
                .body(ex.getMessage());
    }

    /**
     * Gère les erreurs de ressource introuvable (ex : ticket ou user avec cet ID n'existe pas).
     * Retourne HTTP 404 Not Found.
     *
     * @param ex L'exception avec l'ID de la ressource manquante
     * @return ResponseEntity avec le message d'erreur et statut 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)  // 404 : ressource introuvable
                .body(ex.getMessage());
    }

    /**
     * Gère les erreurs d'accès refusé (ex : un USER tente d'accéder au ticket d'un autre).
     * Retourne HTTP 403 Forbidden.
     *
     * Capture à la fois :
     * - org.springframework.security.access.AccessDeniedException (Spring Security)
     * - java.lang.SecurityException (exception Java standard)
     *
     * @param ex L'exception d'accès refusé
     * @return ResponseEntity avec le message d'erreur et statut 403
     */
    @ExceptionHandler({ AccessDeniedException.class, SecurityException.class })
    public ResponseEntity<String> handleAccessDeniedException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)  // 403 : accès refusé (authentifié mais non autorisé)
                .body(ex.getMessage());
    }

    /**
     * Gère les erreurs de validation des arguments (ex : mot de passe actuel incorrect).
     * Retourne HTTP 400 Bad Request.
     *
     * @param ex L'exception contenant le message d'erreur métier
     * @return ResponseEntity avec le message et statut 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 400 : paramètre invalide
                .body(ex.getMessage());
    }

    /**
     * Gère les erreurs de validation Bean Validation (@Valid sur les DTOs).
     * Retourne HTTP 400 Bad Request avec un map { nomChamp → messageErreur }.
     *
     * Exemple de réponse :
     * { "title": "Le titre du ticket est obligatoire", "category": "La catégorie est obligatoire" }
     *
     * @param ex L'exception contenant les erreurs de champs
     * @return ResponseEntity avec la map des erreurs par champ et statut 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        // Construire une map { nomDuChamp → messageErreur } pour chaque champ invalide
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Gère les erreurs métier génériques (ex : email déjà utilisé, compte désactivé).
     * Retourne HTTP 409 Conflict.
     *
     * ⚠️  Ce handler est en dernier recours pour les RuntimeException non spécifiquement
     * capturées par les handlers précédents. Il est placé EN DERNIER pour ne pas
     * "avaler" des exceptions qui devraient être gérées plus précisément.
     *
     * @param ex L'exception RuntimeException générique
     * @return ResponseEntity avec le message et statut 409
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)  // 409 : conflit (ex : contrainte d'unicité violée)
                .body(ex.getMessage());
    }
}
