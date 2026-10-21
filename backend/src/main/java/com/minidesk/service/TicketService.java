package com.minidesk.service;

import com.minidesk.dto.request.TicketRequest;
import com.minidesk.dto.response.TicketResponse;
import com.minidesk.entity.User;

import java.util.List;
import java.util.Map;

/**
 * Interface du service de gestion des tickets.
 * Définit le contrat métier implémenté par TicketServiceImpl.
 */
public interface TicketService {

    /**
     * Créer un nouveau ticket.
     * Le statut initial est OPEN (géré par @PrePersist sur l'entité).
     * @param request    - Données du ticket à créer
     * @param currentUser - Utilisateur connecté (créateur du ticket)
     * @return Le ticket créé avec son ID
     */
    TicketResponse createTicket(TicketRequest request, User currentUser);

    /**
     * Récupérer tous les tickets.
     * Comportement adapté selon le rôle de l'utilisateur :
     *   - ADMIN/TECHNICIAN → tous les tickets
     *   - USER             → uniquement ses tickets
     * @param currentUser - Utilisateur connecté
     */
    List<TicketResponse> getAllTickets(User currentUser);

    /**
     * Récupérer un ticket par son identifiant.
     * @param id         - ID du ticket
     * @param currentUser - Utilisateur connecté (vérification des droits d'accès)
     */
    TicketResponse getTicketById(Long id, User currentUser);

    /**
     * Mettre à jour un ticket (admin/tech : tout champ ; user : description seulement si OPEN).
     * @param id         - ID du ticket à modifier
     * @param request    - Nouvelles données
     * @param currentUser - Utilisateur connecté
     */
    TicketResponse updateTicket(Long id, TicketRequest request, User currentUser);

    /**
     * Supprimer un ticket.
     * Réservé aux ADMIN uniquement (contrôlé par @PreAuthorize dans le contrôleur).
     * @param id - ID du ticket à supprimer
     */
    void deleteTicket(Long id);

    /**
     * Retourner les statistiques agrégées pour le dashboard.
     * Inclut les comptages par statut, le total, etc.
     * @return Map<String, Object> avec les données du dashboard
     */
    Map<String, Object> getDashboardStats(User currentUser);
}
