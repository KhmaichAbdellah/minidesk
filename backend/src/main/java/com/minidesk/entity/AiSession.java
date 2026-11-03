package com.minidesk.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant une session de chat avec l'assistant IA.
 *
 * Mappée vers la table "ai_sessions" en base de données PostgreSQL.
 * Stocke les échanges (tableau JSON de messages user/assistant) dans une colonne JSONB.
 */
@Entity
@Table(name = "ai_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSession {

    /** Identifiant unique de la session IA. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** L'utilisateur ayant initié la session avec l'IA. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Le ticket éventuellement créé ou associé à cette session IA. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", unique = true)
    private Ticket ticket;

    /** Historique des messages au format JSONB sous PostgreSQL. */
    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode messages;

    /** Indique si le problème de l'utilisateur a été résolu directement par l'IA. */
    @Column(nullable = false)
    private Boolean resolvedByAi;

    /** Date et heure d'ouverture de la session IA. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}