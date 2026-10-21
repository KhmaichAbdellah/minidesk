package com.minidesk.dto.response;

import com.minidesk.enums.Priority;
import com.minidesk.enums.TicketCategory;
import com.minidesk.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO retourné par les endpoints de ticket.
 * Contient toutes les informations nécessaires à l'affichage côté frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    // Identifiant unique du ticket en base de données
    private Long id;

    // Titre du ticket
    private String title;

    // Description détaillée du problème
    private String description;

    // Catégorie : NETWORK | HARDWARE | SOFTWARE | ACCESS | OTHER
    private TicketCategory category;

    // Priorité : BASSE | NORMALE | HAUTE | CRITIQUE
    private Priority priority;

    // Statut actuel : OPEN | PROGRESS | RESOLVED | CLOSED
    private TicketStatus status;

    // Identifiant de l'utilisateur qui a créé le ticket
    private Long createdById;

    // Nom complet de l'utilisateur qui a créé le ticket
    private String createdByName;

    // Email de l'utilisateur qui a créé le ticket
    private String createdByEmail;

    // Identifiant de l'utilisateur assigné (null si non assigné)
    private Long assignedToId;

    // Nom du technicien/utilisateur assigné (null si non assigné)
    private String assignedToName;

    // Date de création du ticket (générée automatiquement)
    private LocalDateTime createdAt;

    // Date de dernière modification (mise à jour automatiquement)
    private LocalDateTime updatedAt;
}
