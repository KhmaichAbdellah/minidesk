package com.minidesk.repository;

import com.minidesk.entity.Ticket;
import com.minidesk.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository JPA pour l'entité Ticket.
 * Fournit les opérations CRUD standard + requêtes métier spécifiques.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Trouver tous les tickets créés par un utilisateur donné.
     * Utilisé par les users avec le rôle USER pour voir uniquement leurs tickets.
     * @param userId - Identifiant de l'utilisateur créateur
     */
    List<Ticket> findByCreatedById(Long userId);

    /**
     * Trouver tous les tickets assignés à un technicien donné.
     * Utilisé pour le tableau de bord du technicien.
     * @param techId - Identifiant du technicien assigné
     */
    List<Ticket> findByAssignedToId(Long techId);

    /**
     * Compter le nombre de tickets par statut.
     * Utilisé pour les statistiques du dashboard.
     * @param status - Le statut à compter (OPEN, PROGRESS, RESOLVED, CLOSED)
     */
    long countByStatus(TicketStatus status);

    /**
     * Compter les tickets d'un utilisateur donné par statut.
     * Utilisé pour les statistiques du dashboard côté USER.
     */
    long countByStatusAndCreatedById(TicketStatus status, Long userId);

    /**
     * Compter tous les tickets créés par un utilisateur donné.
     */
    long countByCreatedById(Long userId);

    /**
     * Requête JPQL pour les statistiques du dashboard :
     * retourne une liste de paires [statut, nombre].
     */
    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countByStatusGrouped();
}
