package com.minidesk.controller;

import com.minidesk.dto.request.TicketRequest;
import com.minidesk.dto.response.TicketResponse;
import com.minidesk.entity.User;
import com.minidesk.service.TicketService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des tickets de support.
 * Base URL : /api/tickets
 *
 * Droits d'accès :
 *   - GET    /api/tickets         → Tous les utilisateurs authentifiés (filtré par rôle)
 *   - GET    /api/tickets/{id}    → Tous les utilisateurs authentifiés (filtré par rôle)
 *   - POST   /api/tickets         → Tous les utilisateurs authentifiés
 *   - PUT    /api/tickets/{id}    → ADMIN ou TECHNICIAN
 *   - DELETE /api/tickets/{id}    → ADMIN uniquement
 *   - GET    /api/tickets/stats   → ADMIN ou TECHNICIAN (dashboard)
 */
@Tag(name = "🎫 Tickets", description = "CRUD des tickets de support — accès adapté selon le rôle (USER / TECHNICIAN / ADMIN)")
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final com.minidesk.service.CommentService commentService;

    // ── GET /api/tickets/{id}/comments ─────────────────────────────────────────
    @Operation(summary = "Lister les commentaires d'un ticket",
        description = "Retourne tous les commentaires déposés sur un ticket dans l'ordre chronologique.")
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<com.minidesk.dto.response.CommentResponse>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(commentService.getCommentsByTicket(id, currentUser));
    }

    // ── POST /api/tickets/{id}/comments ────────────────────────────────────────
    @Operation(summary = "Ajouter un commentaire à un ticket",
        description = "Ajoute un nouveau message/commentaire au ticket spécifié.")
    @PostMapping("/{id}/comments")
    public ResponseEntity<com.minidesk.dto.response.CommentResponse> addComment(
            @PathVariable Long id,
            @Valid @RequestBody com.minidesk.dto.request.CommentRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(id, request, currentUser));
    }

    // ── GET /api/tickets/{id}/history ─────────────────────────────────────────
    @Operation(summary = "Obtenir l'historique des statuts d'un ticket",
        description = "Retourne la liste chronologique de toutes les transitions de statut subies par le ticket (StatutLog).")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<com.minidesk.dto.response.StatutLogResponse>> getTicketHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ticketService.getTicketHistory(id, currentUser));
    }

    // ── GET /api/tickets ───────────────────────────────────────────────────────
    @Operation(summary = "Lister les tickets",
        description = "Retourne tous les tickets visibles par l'utilisateur connecté. Les ADMIN/TECHNICIAN voient tout, les USER voient uniquement leurs tickets.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste retournée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ticketService.getAllTickets(currentUser));
    }

    // ── GET /api/tickets/{id} ─────────────────────────────────────────────────
    @Operation(summary = "Obtenir un ticket par ID",
        description = "Retourne les détails d'un ticket. Un USER ne peut voir que ses propres tickets.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket trouvé"),
        @ApiResponse(responseCode = "403", description = "Accès refusé à ce ticket"),
        @ApiResponse(responseCode = "404", description = "Ticket introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @Parameter(description = "ID du ticket", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ticketService.getTicketById(id, currentUser));
    }

    // ── POST /api/tickets ─────────────────────────────────────────────────────
    @Operation(summary = "Créer un ticket",
        description = "Crée un nouveau ticket de support. Le statut initial est automatiquement OPEN.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketRequest request,
            @AuthenticationPrincipal User currentUser) {
        TicketResponse created = ticketService.createTicket(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── PUT /api/tickets/{id} ─────────────────────────────────────────────────
    @Operation(summary = "Mettre à jour un ticket",
        description = "Met à jour un ticket. Les ADMIN/TECHNICIAN peuvent modifier tous les champs. Un USER ne peut modifier que ses tickets OPEN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket mis à jour"),
        @ApiResponse(responseCode = "403", description = "Modification non autorisée"),
        @ApiResponse(responseCode = "404", description = "Ticket introuvable")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @Parameter(description = "ID du ticket", example = "1") @PathVariable Long id,
            @Valid @RequestBody TicketRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request, currentUser));
    }

    // ── DELETE /api/tickets/{id} ──────────────────────────────────────────────
    @Operation(summary = "Supprimer un ticket", description = "Supprime définitivement un ticket. Réservé aux ADMIN uniquement.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ticket supprimé"),
        @ApiResponse(responseCode = "403", description = "Accès réservé aux ADMIN")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(
            @Parameter(description = "ID du ticket à supprimer", example = "1") @PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/tickets/stats ────────────────────────────────────────────────
    @Operation(summary = "Statistiques dashboard",
        description = "Retourne les statistiques agrégées (comptages par statut, total, etc.) pour le tableau de bord. ADMIN/TECHNICIAN voient toutes les stats, USER voit uniquement ses propres tickets.")
    @ApiResponse(responseCode = "200", description = "Statistiques retournées")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ticketService.getDashboardStats(currentUser));
    }
}
