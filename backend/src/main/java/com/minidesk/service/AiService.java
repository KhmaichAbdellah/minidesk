package com.minidesk.service;

import com.minidesk.dto.request.AiRequest;
import com.minidesk.dto.request.CreateTicketRequest;
import com.minidesk.dto.response.AiResponse;
import com.minidesk.dto.response.TicketGenerationResult;
import com.minidesk.dto.response.TicketResponse;
import com.minidesk.entity.AiSession;
import com.minidesk.entity.User;

/**
 * Interface du service pour les fonctionnalités d'Intelligence Artificielle.
 * Les implémentations interrogent le fournisseur LLM (Ollama), persistent l'historique
 * dans AiSession et retournent une réponse structurée {@link AiResponse} pour le frontend.
 */
public interface AiService {

    /**
     * Traite un message de chat envoyé par un utilisateur, avec le contexte optionnel d'un ticket.
     *
     * @param user    L'utilisateur effectuant la demande
     * @param request La requête contenant le message et le sessionId / ticketId
     * @return Réponse structurée {@link AiResponse} contenant la réponse de l'assistant
     */
    AiResponse chat(User user, AiRequest request);

    /**
     * Analyse l'intégralité de l'historique d'une session IA et demande à MILO de générer
     * automatiquement un titre, une description synthétique, une catégorie et une priorité
     * pour le ticket correspondant.
     *
     * <p>La description produite est destinée au technicien et contient :
     * le problème principal, les actions proposées par MILO, les actions réalisées par
     * l'utilisateur, les résultats de chaque tentative et l'état actuel du problème.
     *
     * @param session La session IA dont l'historique sera analysé
     * @return {@link TicketGenerationResult} contenant les informations générées par l'IA
     */
    TicketGenerationResult generateTicketInformation(AiSession session);

    /**
     * Crée un ticket à partir d'une session IA existante.
     *
     * <p>Si {@code req.description} est absent (null ou vide), appelle
     * {@link #generateTicketInformation(AiSession)} pour générer automatiquement
     * la description, la catégorie et la priorité à partir de toute la conversation.
     * Les champs fournis manuellement dans {@code req} prennent toujours la priorité.
     *
     * @param session La session IA source
     * @param req     La requête optionnelle du frontend (peut être vide)
     * @param user    L'utilisateur authentifié créant le ticket
     * @return {@link TicketResponse} représentant le ticket persisté
     */
    TicketResponse createTicketFromSession(AiSession session, CreateTicketRequest req, User user);
}
