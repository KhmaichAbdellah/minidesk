package com.minidesk.service.impl;

import com.minidesk.dto.request.TicketRequest;
import com.minidesk.dto.response.TicketResponse;
import com.minidesk.entity.StatutLog;
import com.minidesk.entity.Ticket;
import com.minidesk.entity.User;
import com.minidesk.enums.Role;
import com.minidesk.enums.TicketStatus;
import com.minidesk.exception.ResourceNotFoundException;
import com.minidesk.repository.StatusLogRepository;
import com.minidesk.repository.TicketRepository;
import com.minidesk.repository.UserRepository;
import com.minidesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    // Repository JPA pour toutes les opérations CRUD sur les tickets
    private final TicketRepository ticketRepository;

    // Repository JPA pour résoudre l'ID du technicien assigné en entité User
    private final UserRepository userRepository;

    // Repository JPA pour persister les logs de changement de statut
    private final StatusLogRepository statusLogRepository;

    // ────────────────────────────────────────────────────────────────────────
    // MAPPER : Ticket (entité JPA) → TicketResponse (DTO envoyé au client)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Convertit une entité JPA {@link Ticket} en DTO {@link TicketResponse}.
     *
     * Pourquoi un DTO et pas l'entité directement ?
     *   - Évite la sérialisation des relations JPA lazy (LazyInitializationException)
     *   - Évite les boucles infinies JSON (Ticket → User → tickets → Ticket...)
     *   - Contrôle précis des données exposées au client
     *
     * @param ticket L'entité Ticket chargée depuis la base de données
     * @return DTO TicketResponse prêt à être sérialisé en JSON
     */
    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())                                 // ID unique du ticket en base
                .title(ticket.getTitle())                           // Titre court du ticket
                .description(ticket.getDescription())               // Description détaillée
                .category(ticket.getCategory())                     // Enum TicketCategory (SOFTWARE, HARDWARE, etc.)
                .priority(ticket.getPriority())                     // Enum Priority (BASSE, NORMALE, HAUTE, CRITIQUE)
                .status(ticket.getStatus())                         // Enum TicketStatus (OPEN, PROGRESS, RESOLVED, CLOSED)
                // ── Informations du créateur (toujours non-null) ──
                .createdById(ticket.getCreatedBy().getId())         // ID de l'utilisateur créateur
                .createdByName(ticket.getCreatedBy().getName())     // Nom complet du créateur
                .createdByEmail(ticket.getCreatedBy().getEmail())   // Email du créateur
                // ── Informations du technicien assigné (peut être null si non assigné) ──
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getName() : null)
                // ── Timestamps gérés automatiquement par Hibernate ──
                .createdAt(ticket.getCreatedAt())                   // Date de création (@CreationTimestamp)
                .updatedAt(ticket.getUpdatedAt())                   // Date de dernière modification (@UpdateTimestamp)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // CRÉER UN TICKET
    // POST /api/tickets
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Crée un nouveau ticket de support.
     *
     * L'utilisateur connecté devient automatiquement le créateur (createdBy).
     * L'assignation est optionnelle et réservée aux admins/techniciens.
     *
     * @param request     DTO contenant les données du ticket (titre, catégorie, priorité, etc.)
     * @param currentUser L'utilisateur connecté (injecté par @AuthenticationPrincipal)
     * @return DTO TicketResponse du ticket créé (avec son ID généré)
     */
    @Override
    public TicketResponse createTicket(TicketRequest request, User currentUser) {

        // ── Résoudre l'utilisateur assigné si un ID est fourni ───────────────
        // findById retourne un Optional → on lève une exception si l'ID fourni n'existe pas
        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Technicien non trouvé avec l'id : " + request.getAssignedToId()));
        }

        // ── Construire l'entité Ticket à partir du DTO ────────────────────────
        // Le pattern Builder (généré par Lombok @Builder sur Ticket) permet
        // une construction fluide et lisible de l'entité
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                // Le statut est OPEN par défaut (via @PrePersist sur Ticket),
                // mais le DTO peut le surcharger si fourni
                .status(request.getStatus() != null ? request.getStatus() : TicketStatus.OPEN)
                .createdBy(currentUser)   // L'utilisateur connecté est le créateur
                .assignedTo(assignedTo)   // Null si pas d'assignation
                .build();

        // ── Persister en base et retourner le DTO ─────────────────────────────
        Ticket saved = ticketRepository.save(ticket);  // INSERT SQL + génération de l'ID
        return toResponse(saved);
    }

    // ────────────────────────────────────────────────────────────────────────
    // LISTER LES TICKETS (filtré selon le rôle)
    // GET /api/tickets
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Retourne la liste des tickets accessible à l'utilisateur connecté.
     *
     * Règles de filtrage par rôle :
     *   ADMIN / TECHNICIAN → tous les tickets en base
     *   USER               → uniquement les tickets qu'il a créés
     *
     * @param currentUser L'utilisateur connecté
     * @return Liste de TicketResponse (peut être vide mais jamais null)
     */
    @Override
    public List<TicketResponse> getAllTickets(User currentUser) {
        List<Ticket> tickets;

        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.TECHNICIAN) {
            // ADMIN et TECHNICIAN voient tous les tickets → SELECT * FROM tickets
            tickets = ticketRepository.findAll();
        } else {
            // USER voit uniquement ses propres tickets → WHERE created_by_id = ?
            tickets = ticketRepository.findByCreatedById(currentUser.getId());
        }

        // Convertir chaque entité Ticket en DTO TicketResponse via le mapper
        return tickets.stream()
                .map(this::toResponse)        // this::toResponse = référence à la méthode privée toResponse()
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────────────────
    // RÉCUPÉRER UN TICKET PAR ID
    // GET /api/tickets/{id}
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Retourne un ticket spécifique par son ID.
     *
     * Contrôle d'accès :
     *   ADMIN / TECHNICIAN → peuvent voir n'importe quel ticket
     *   USER               → ne peut voir que ses propres tickets
     *
     * @param id          L'ID du ticket à récupérer
     * @param currentUser L'utilisateur connecté
     * @return DTO TicketResponse du ticket
     * @throws ResourceNotFoundException si le ticket n'existe pas
     * @throws AccessDeniedException     si un USER tente d'accéder au ticket d'un autre
     */
    @Override
    public TicketResponse getTicketById(Long id, User currentUser) {
        // Chercher le ticket en base → lève ResourceNotFoundException si introuvable (→ HTTP 404)
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket non trouvé avec l'id : " + id));

        // Vérification d'accès : un USER ne peut voir que ses propres tickets
        if (currentUser.getRole() == Role.USER
                && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            // AccessDeniedException est capturée par GlobalExceptionHandler → HTTP 403
            throw new AccessDeniedException("Accès refusé : ce ticket ne vous appartient pas");
        }

        return toResponse(ticket);
    }

    // ────────────────────────────────────────────────────────────────────────
    // METTRE À JOUR UN TICKET
    // PUT /api/tickets/{id}
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Met à jour les champs d'un ticket existant.
     *
     * Règles de modification par rôle :
     *   USER               → peut modifier titre, description, catégorie, priorité de SES tickets
     *                        NE PEUT PAS changer le statut ni l'assignation
     *   ADMIN / TECHNICIAN → peuvent tout modifier (statut, assignation, etc.)
     *
     * Effet de bord : si le statut change, un {@link StatutLog} est enregistré
     * pour tracer l'historique des changements de statut.
     *
     * @param id          L'ID du ticket à modifier
     * @param request     DTO contenant les nouvelles valeurs
     * @param currentUser L'utilisateur connecté
     * @return DTO TicketResponse mis à jour
     * @throws ResourceNotFoundException si le ticket n'existe pas
     * @throws AccessDeniedException     si un USER tente de modifier le ticket d'un autre
     */
    @Override
    public TicketResponse updateTicket(Long id, TicketRequest request, User currentUser) {
        // ── Vérifier l'existence du ticket ────────────────────────────────────
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket non trouvé avec l'id : " + id));

        // ── Contrôle d'accès : un USER ne peut modifier que ses propres tickets ──
        if (currentUser.getRole() == Role.USER
                && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé : modification non autorisée");
        }

        // ── Mise à jour des champs de base (accessibles à tous les rôles) ─────
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());

        // ── Changement de statut (ADMIN et TECHNICIAN uniquement) ─────────────
        if (request.getStatus() != null &&
                (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.TECHNICIAN)) {
            TicketStatus oldStatus = ticket.getStatus();  // Statut avant modification
            TicketStatus newStatus = request.getStatus(); // Nouveau statut demandé

            // N'enregistrer un log que si le statut change réellement
            if (oldStatus != newStatus) {
                ticket.setStatus(newStatus);

                // ── Enregistrer le log de changement de statut ───────────────
                // StatutLog trace : qui a changé quoi, quand (via @CreationTimestamp)
                StatutLog statutLog = StatutLog.builder()
                        .ticket(ticket)          // Le ticket concerné
                        .oldStatus(oldStatus)    // Statut précédent
                        .newStatus(newStatus)    // Nouveau statut
                        .changedBy(currentUser)  // L'utilisateur qui a effectué le changement
                        .build();
                statusLogRepository.save(statutLog);  // INSERT dans la table statut_logs
            }
        }

        // ── Assignation du ticket à un technicien (ADMIN et TECHNICIAN uniquement) ──
        if (request.getAssignedToId() != null &&
                (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.TECHNICIAN)) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Technicien non trouvé avec l'id : " + request.getAssignedToId()));
            ticket.setAssignedTo(assignedTo);  // Lie le technicien au ticket
        }

        // ── Sauvegarder les modifications et retourner le DTO mis à jour ──────
        return toResponse(ticketRepository.save(ticket));  // UPDATE SQL
    }


    @Override
    public void deleteTicket(Long id) {
        // Vérifier que le ticket existe avant suppression pour retourner 404 si introuvable
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket non trouvé avec l'id : " + id);
        }
        ticketRepository.deleteById(id);  // DELETE SQL (avec cascade sur les relations)
    }



    @Override
    public Map<String, Object> getDashboardStats(User currentUser) {
        Map<String, Object> stats = new HashMap<>();

        boolean isAdminOrTech = currentUser.getRole() == Role.ADMIN
                             || currentUser.getRole() == Role.TECHNICIAN;

        if (isAdminOrTech) {
            // ADMIN / TECHNICIAN : statistiques globales sur tous les tickets
            stats.put("total",    ticketRepository.count());
            stats.put("open",     ticketRepository.countByStatus(TicketStatus.OPEN));
            stats.put("progress", ticketRepository.countByStatus(TicketStatus.PROGRESS));
            stats.put("resolved", ticketRepository.countByStatus(TicketStatus.RESOLVED));
            stats.put("closed",   ticketRepository.countByStatus(TicketStatus.CLOSED));
        } else {
            // USER : statistiques limitées à ses propres tickets
            Long userId = currentUser.getId();
            stats.put("total",    ticketRepository.countByCreatedById(userId));
            stats.put("open",     ticketRepository.countByStatusAndCreatedById(TicketStatus.OPEN,     userId));
            stats.put("progress", ticketRepository.countByStatusAndCreatedById(TicketStatus.PROGRESS, userId));
            stats.put("resolved", ticketRepository.countByStatusAndCreatedById(TicketStatus.RESOLVED, userId));
            stats.put("closed",   ticketRepository.countByStatusAndCreatedById(TicketStatus.CLOSED,   userId));
        }

        return stats;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<com.minidesk.dto.response.StatutLogResponse> getTicketHistory(Long id, User currentUser) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable avec l'id : " + id));

        if (currentUser.getRole() == Role.USER) {
            boolean isAuthor = ticket.getCreatedBy() != null && ticket.getCreatedBy().getId().equals(currentUser.getId());
            if (!isAuthor) {
                throw new AccessDeniedException("Accès refusé : vous n'êtes pas l'auteur de ce ticket.");
            }
        }

        return statusLogRepository.findByTicketIdOrderByChangedAtAsc(id).stream()
                .map(log -> com.minidesk.dto.response.StatutLogResponse.builder()
                        .id(log.getId())
                        .ticketId(log.getTicket().getId())
                        .oldStatus(log.getOldStatus())
                        .newStatus(log.getNewStatus())
                        .changedById(log.getChangedBy() != null ? log.getChangedBy().getId() : null)
                        .changedByName(log.getChangedBy() != null ? log.getChangedBy().getName() : "Inconnu")
                        .changedByEmail(log.getChangedBy() != null ? log.getChangedBy().getEmail() : null)
                        .changedAt(log.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
