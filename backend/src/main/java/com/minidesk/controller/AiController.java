package com.minidesk.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.minidesk.dto.request.AiRequest;
import com.minidesk.dto.request.CreateTicketRequest;
import com.minidesk.dto.response.AiResponse;
import com.minidesk.dto.response.AiSessionResponse;
import com.minidesk.dto.response.TicketResponse;
import com.minidesk.entity.AiSession;
import com.minidesk.entity.Ticket;
import com.minidesk.entity.User;
import com.minidesk.enums.TicketStatus;
import com.minidesk.repository.AiSessionRepository;
import com.minidesk.repository.TicketRepository;
import com.minidesk.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Tag(name = " Assistant IA (MILO)", description = "Chat IA multi-tours avec MILO — création automatique de tickets et consultation de l'historique")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiSessionRepository aiSessionRepository;
    private final TicketRepository ticketRepository;


//      Mapper  : Convertit une entité AiSession en DTO AiSessionResponse.

    private AiSessionResponse toSessionResponse(AiSession session) {
        return AiSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUser() != null ? session.getUser().getId() : null)
                .userName(session.getUser() != null ? session.getUser().getName() : null)
                .ticketId(session.getTicket() != null ? session.getTicket().getId() : null)
                .ticketTitle(session.getTicket() != null ? session.getTicket().getTitle() : null)
                .messages(session.getMessages())
                .resolvedByAi(session.getResolvedByAi())
                .createdAt(session.getCreatedAt())
                .build();
    }


    @Operation(summary = "Envoyer un message à MILO",
        description = "Envoie un message à l'assistant IA MILO. Si un `sessionId` est fourni, la conversation continue avec l'historique. Retourne la réponse de l'IA et le nouvel ID de session.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Réponse de l'IA retournée"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PostMapping("/chat")
    public ResponseEntity<AiResponse> chat(@RequestBody AiRequest request) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User user)) {
            return ResponseEntity.status(401).build();
        }

        AiResponse response = aiService.chat(user, request);
        return ResponseEntity.ok(response);
    }



    @Operation(summary = "Créer un ticket depuis une session IA",
        description = "Crée un ticket de support lié à une session de chat IA existante. Si la description est vide, MILO génère automatiquement la synthèse, la catégorie et la priorité à partir de TOUTE la conversation.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket créé et lié à la session"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Session IA introuvable")
    })
    @PostMapping("/{sessionId}/create-ticket")
    public ResponseEntity<TicketResponse> createTicketFromSession(
            @Parameter(description = "ID de la session IA", example = "1") @PathVariable Long sessionId,
            @RequestBody CreateTicketRequest req) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User user)) {
            return ResponseEntity.status(401).build();
        }

        AiSession session = aiSessionRepository.findById(sessionId).orElse(null);

        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        CreateTicketRequest request = req != null ? req : new CreateTicketRequest();
        TicketResponse response = aiService.createTicketFromSession(session, request, user);

        return ResponseEntity.ok(response);
    }




    @Operation(summary = "Obtenir une session IA par ID",
        description = "Récupère l'historique complet des messages d'une session IA (format JSON de la conversation).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session retournée avec son historique"),
        @ApiResponse(responseCode = "404", description = "Session introuvable")
    })
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<AiSessionResponse> getSessionById(
            @Parameter(description = "ID de la session IA", example = "1") @PathVariable Long sessionId) {
        AiSession session = aiSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toSessionResponse(session));
    }



    @Operation(summary = "Obtenir la session IA d'un ticket",
        description = "Récupère la session de chat IA associée à un ticket de support. Utile pour voir la conversation qui a mené à l'ouverture du ticket.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session trouvée"),
        @ApiResponse(responseCode = "404", description = "Aucune session IA pour ce ticket")
    })
    @GetMapping("/tickets/{ticketId}/session")
    public ResponseEntity<AiSessionResponse> getSessionByTicketId(
            @Parameter(description = "ID du ticket", example = "1") @PathVariable Long ticketId) {
        AiSession session = aiSessionRepository.findByTicketId(ticketId).orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toSessionResponse(session));
    }


    @Operation(summary = "Mes sessions IA",
        description = "Retourne toutes les sessions de chat IA de l'utilisateur connecté, triées de la plus récente à la plus ancienne.")
    @ApiResponse(responseCode = "200", description = "Liste de sessions retournée")
    @GetMapping("/my-sessions")
    public ResponseEntity<List<AiSessionResponse>> getMySessions() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User user)) {
            return ResponseEntity.status(401).build();
        }

        List<AiSession> sessions = aiSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<AiSessionResponse> responseList = sessions.stream()
                .map(this::toSessionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}