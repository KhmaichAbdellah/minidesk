package com.minidesk.entity;

import jakarta.persistence.*;
import lombok.*;
import com.minidesk.enums.TicketStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entité JPA pour l'historique des changements de statut de ticket.
 *
 * Mappée vers la table "statut_logs" en base de données PostgreSQL.
 * Permet de conserver une trace d'audit (qui a changé le statut, quand, et de quel statut vers quel statut).
 */
@Entity
@Table(name = "statut_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutLog {

    /** Identifiant unique de l'entrée de journal. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Le ticket dont le statut a été modifié. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Ancien statut du ticket avant modification. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus oldStatus;

    /** Nouveau statut du ticket après modification. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus newStatus;

    /** L'utilisateur ou le technicien qui a modifié le statut. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    /** Horodatage automatique du changement de statut. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;
}