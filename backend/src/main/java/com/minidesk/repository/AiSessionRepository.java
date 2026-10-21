package com.minidesk.repository;

import com.minidesk.entity.AiSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour l'entité AiSession (sessions de chat avec l'assistant IA).
 */
public interface AiSessionRepository extends JpaRepository<AiSession, Long> {

    /**
     * Trouver toutes les sessions IA d'un utilisateur triées par date de création décroissante.
     *
     * @param userId L'ID de l'utilisateur
     * @return Liste de sessions AiSession
     */
    List<AiSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Trouver la session IA associée à un ticket spécifique.
     *
     * @param ticketId L'ID du ticket
     * @return Optional contenant l'AiSession si elle existe
     */
    Optional<AiSession> findByTicketId(Long ticketId);
}
