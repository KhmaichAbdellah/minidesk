package com.minidesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant un commentaire déposé sur un ticket de support.
 *
 * Mappée vers la table "comments" en base de données PostgreSQL.
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    /** Identifiant unique du commentaire. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Le ticket auquel est rattaché ce commentaire. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** L'utilisateur auteur du commentaire. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** Contenu textuel du commentaire. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** Date et heure de création du commentaire (générée automatiquement). */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}