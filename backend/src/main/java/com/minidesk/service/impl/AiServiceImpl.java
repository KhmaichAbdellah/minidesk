package com.minidesk.service.impl;

import com.minidesk.dto.request.AiRequest;
import com.minidesk.dto.request.CreateTicketRequest;
import com.minidesk.dto.response.AiResponse;
import com.minidesk.dto.response.TicketGenerationResult;
import com.minidesk.dto.response.TicketResponse;
import com.minidesk.entity.AiSession;
import com.minidesk.entity.Ticket;
import com.minidesk.entity.User;
import com.minidesk.enums.Priority;
import com.minidesk.enums.TicketCategory;
import com.minidesk.enums.TicketStatus;
import com.minidesk.repository.AiSessionRepository;
import com.minidesk.repository.TicketRepository;
import com.minidesk.service.AiService;
import com.minidesk.service.llm.LlmClient;
import com.minidesk.service.llm.LlmProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.minidesk.service.llm.PromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation du service d'interaction avec l'IA (LLM Ollama).
 *
 * Responsabilités :
 *   1. Récupère la session IA existante si un sessionId est fourni pour maintenir la mémoire contextuelle.
 *   2. Construit le prompt avec le contexte complet (utilisateur, ticket, historique) via {@link PromptBuilder}.
 *   3. Envoie la requête au client LLM (Ollama).
 *   4. Extrait et nettoie le texte de réponse en éliminant les artéfacts et balises brutes.
 *   5. Déclenche la création d'un ticket résolu UNIQUEMENT si l'utilisateur confirme explicitement la résolution.
 *   6. Met à jour et sauvegarde la session {@link AiSession}.
 *   7. Génère automatiquement les informations d'un ticket (description, category, priority) à partir de
 *      l'historique complet d'une session en interrogeant MILO avec un prompt spécialisé.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final AiSessionRepository aiSessionRepository;
    private final TicketRepository ticketRepository;
    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final LlmProperties llmProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ──────────────────────────────────────────────────────────────────────────
    // chat : multi-tour avec MILO
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public AiResponse chat(User user, AiRequest request) {
        long startTimeMs = System.currentTimeMillis();
        int ticketCount = 0;

        try {
            // ── Étape 1 : Récupérer le ticket et la session existante si des IDs sont fournis ──
            Ticket ticket = null;
            if (request.getTicketId() != null) {
                ticket = ticketRepository.findById(request.getTicketId()).orElse(null);
            }

            AiSession session = null;
            if (request.getSessionId() != null) {
                session = aiSessionRepository.findById(request.getSessionId()).orElse(null);
            }

            // ── Étape 2 : Construire le prompt avec l'historique complet ──────────
            String prompt = promptBuilder.build(user, request, ticket, session);
            String rawResponse = llmClient.sendPrompt(prompt);
            log.info("Réponse brute Ollama : {}", rawResponse);

            // ── Étape 3 : Extraire le texte de la réponse (message.content) ───────
            String contentText = extractMessageContent(rawResponse);

            // ── Étape 4 : Nettoyer le contenu et vérifier si l'utilisateur confirme la résolution ──
            ParsedAiContent parsed = parseContentText(contentText);

            String replyText = parsed.replyText;

            // La résolution n'est validée QUE si l'utilisateur l'affirme explicitement dans son message
            boolean resolved = isUserConfirmingResolution(request.getMessage());
            boolean createTicketSuggested = parsed.createTicketSuggested || isUserRequestingTicket(request.getMessage());

            String suggestedTitle = parsed.suggestedTitle;
            String suggestedDescription = parsed.suggestedDescription;

            // ── Étape 5 : Création automatique d'un ticket résolu si l'utilisateur a confirmé la résolution ──
            Ticket createdTicket = null;
            if (resolved) {
                Ticket t = Ticket.builder()
                        .title(suggestedTitle != null ? suggestedTitle : defaultResolvedTitle(request.getMessage()))
                        .description(suggestedDescription != null ? suggestedDescription : replyText)
                        .category(TicketCategory.OTHER)
                        .priority(Priority.NORMALE)
                        .status(TicketStatus.RESOLVED)
                        .createdBy(user)
                        .build();
                createdTicket = ticketRepository.save(t);
                ticketCount = 1;
                log.info("Ticket automatiquement créé par l'IA (résolu par l'utilisateur) : id={}, title={}", createdTicket.getId(), createdTicket.getTitle());
            }

            // ── Étape 6 : Accumuler l'historique dans la session AiSession ──────
            List<Map<String, String>> conversationMessages = new ArrayList<>();
            if (session != null && session.getMessages() != null && session.getMessages().isArray()) {
                for (JsonNode node : session.getMessages()) {
                    if (node.has("role") && node.has("content")) {
                        Map<String, String> m = new HashMap<>();
                        m.put("role", node.get("role").asText());
                        m.put("content", node.get("content").asText());
                        conversationMessages.add(m);
                    }
                }
            }

            // Ajouter le nouveau message utilisateur et la réponse nettoyée de l'assistant
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", request.getMessage());
            conversationMessages.add(userMsg);

            Map<String, String> assistantMsg = new HashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", replyText);
            conversationMessages.add(assistantMsg);

            JsonNode jsonMessages = objectMapper.valueToTree(conversationMessages);

            if (session == null) {
                session = AiSession.builder()
                        .user(user)
                        .ticket(createdTicket)
                        .messages(jsonMessages)
                        .resolvedByAi(resolved)
                        .build();
            } else {
                session.setMessages(jsonMessages);
                if (createdTicket != null) {
                    session.setTicket(createdTicket);
                }
                session.setResolvedByAi(resolved);
            }

            session = aiSessionRepository.save(session);

            long elapsedMs = System.currentTimeMillis() - startTimeMs;
            log.info("Chat IA terminé : sessionId={}, elapsedMs={}, resolved={}, ticketCount={}",
                    session.getId(), elapsedMs, resolved, ticketCount);

            // ── Étape 7 : Construire le DTO de réponse pour le frontend ──────────
            AiResponse aiResponse = new AiResponse();
            aiResponse.setReply(replyText);
            aiResponse.setSessionId(session.getId());
            aiResponse.setResolvedByAi(resolved);
            aiResponse.setSuggestedCreateTicket(!resolved && createTicketSuggested);
            aiResponse.setSuggestedTicketTitle(suggestedTitle != null ? suggestedTitle : "Ticket d'assistance IT");
            aiResponse.setSuggestedTicketDescription(suggestedDescription != null ? suggestedDescription : replyText);

            return aiResponse;

        } catch (Exception e) {
            log.error("Erreur lors de l'appel au service IA :", e);

            AiResponse err = new AiResponse();
            err.setReply("Désolé, une erreur est survenue lors de la communication avec l'assistant IA.");
            err.setSessionId(null);
            err.setResolvedByAi(false);
            err.setSuggestedCreateTicket(false);
            return err;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // generateTicketInformation : analyse TOUTE la session et génère titre,
    // description, category, priority via un prompt JSON strict
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public TicketGenerationResult generateTicketInformation(AiSession session) {
        log.info("Génération automatique des informations du ticket pour la session id={}",
                session != null ? session.getId() : "null");

        // Valeurs par défaut robustes utilisées si la session est vide ou si l'IA échoue
        final String defaultTitle       = "Demande d'assistance IT";
        final String defaultDescription = "Aucun historique de conversation disponible pour générer une description automatique.";
        final TicketCategory defaultCategory = TicketCategory.OTHER;
        final Priority defaultPriority       = Priority.NORMALE;

        // Session vide ou sans messages : retourner les valeurs par défaut
        if (session == null
                || session.getMessages() == null
                || !session.getMessages().isArray()
                || session.getMessages().isEmpty()) {
            log.warn("Session vide ou sans messages — utilisation des valeurs par défaut pour le ticket.");
            return new TicketGenerationResult(defaultTitle, defaultDescription, defaultCategory, defaultPriority);
        }

        try {
            // ── Étape 1 : Construire le prompt spécialisé pour la génération de ticket ──
            String prompt = promptBuilder.buildTicketGenerationPrompt(session);
            log.debug("Prompt de génération de ticket envoyé à l'IA (longueur={})", prompt.length());

            // ── Étape 2 : Appeler le LLM ──────────────────────────────────────────
            String rawResponse = llmClient.sendPrompt(prompt);
            log.info("Réponse brute Ollama (génération ticket) : {}", rawResponse);

            // ── Étape 3 : Extraire le contenu texte de la réponse du LLM ──────────
            String contentText = extractMessageContent(rawResponse);

            // ── Étape 4 : Extraire le JSON entre le premier { et le dernier } ──────
            String jsonPayload = extractJsonObject(contentText);
            if (jsonPayload == null) {
                log.warn("Aucun objet JSON trouvé dans la réponse IA. Utilisation des valeurs par défaut.");
                return new TicketGenerationResult(defaultTitle, defaultDescription, defaultCategory, defaultPriority);
            }

            // ── Étape 5 : Parser le JSON ──────────────────────────────────────────
            JsonNode node = objectMapper.readTree(jsonPayload);

            String title = getTextOrDefault(node, "title", defaultTitle);
            String description = getTextOrDefault(node, "description", defaultDescription);

            // ── Étape 6 : Convertir category avec fallback loggé ──────────────────
            TicketCategory category = defaultCategory;
            if (node.has("category") && !node.get("category").isNull()) {
                String rawCategory = node.get("category").asText().trim().toUpperCase();
                try {
                    category = TicketCategory.valueOf(rawCategory);
                } catch (IllegalArgumentException e) {
                    log.warn("Valeur de category invalide retournée par l'IA : '{}'. Fallback → OTHER", rawCategory);
                }
            }

            // ── Étape 7 : Convertir priority avec fallback loggé ──────────────────
            Priority priority = defaultPriority;
            if (node.has("priority") && !node.get("priority").isNull()) {
                String rawPriority = node.get("priority").asText().trim().toUpperCase();
                try {
                    priority = Priority.valueOf(rawPriority);
                } catch (IllegalArgumentException e) {
                    log.warn("Valeur de priority invalide retournée par l'IA : '{}'. Fallback → NORMALE", rawPriority);
                }
            }

            log.info("Ticket généré par l'IA — title='{}', category={}, priority={}", title, category, priority);
            return new TicketGenerationResult(title, description, category, priority);

        } catch (Exception e) {
            log.error("Erreur lors de la génération des informations du ticket par l'IA :", e);
            return new TicketGenerationResult(defaultTitle, defaultDescription, defaultCategory, defaultPriority);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // createTicketFromSession : crée le ticket, délègue à generateTicketInformation
    // si les champs ne sont pas fournis manuellement
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public TicketResponse createTicketFromSession(AiSession session, CreateTicketRequest req, User user) {
        log.info("Création d'un ticket depuis la session id={} par l'utilisateur id={}", session.getId(), user.getId());

        // Déterminer si la génération IA est nécessaire
        // (quand la description n'est pas fournie manuellement)
        boolean needsAiGeneration = req.getDescription() == null || req.getDescription().isBlank();

        String title       = req.getTitle();
        String description = req.getDescription();
        TicketCategory category = req.getCategory();
        Priority priority  = req.getPriority();

        if (needsAiGeneration) {
            log.info("Description absente — lancement de la génération IA pour la session id={}", session.getId());
            TicketGenerationResult generated = generateTicketInformation(session);

            // La description est toujours prise depuis l'IA si elle n'est pas fournie
            description = generated.description();

            // Title, category, priority : l'IA s'applique SEULEMENT si non fourni manuellement
            if (title == null || title.isBlank()) {
                title = generated.title();
            }
            // CreateTicketRequest initialise category=OTHER et priority=NORMALE par défaut ;
            // on surcharge avec les valeurs IA uniquement si elles sont les valeurs par défaut
            // ou si la requête n'avait pas de valeurs explicites.
            // Note : comme CreateTicketRequest.category et priority sont initialisés avec des
            // valeurs par défaut côté Java, on se base sur needsAiGeneration pour les surcharger.
            category = generated.category();
            priority = generated.priority();
        }

        // Construire le ticket
        Ticket ticket = Ticket.builder()
                .title(title != null && !title.isBlank() ? title : "Issue from AI session")
                .description(description != null ? description : "")
                .category(category != null ? category : TicketCategory.OTHER)
                .priority(priority != null ? priority : Priority.NORMALE)
                .status(TicketStatus.OPEN)
                .createdBy(user)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket id={} créé avec succès — category={}, priority={}", savedTicket.getId(), savedTicket.getCategory(), savedTicket.getPriority());

        // Lier la session au ticket créé
        session.setTicket(savedTicket);
        session.setResolvedByAi(false);
        aiSessionRepository.save(session);

        // Construire et retourner le TicketResponse
        return TicketResponse.builder()
                .id(savedTicket.getId())
                .title(savedTicket.getTitle())
                .description(savedTicket.getDescription())
                .category(savedTicket.getCategory())
                .priority(savedTicket.getPriority())
                .status(savedTicket.getStatus())
                .createdById(user.getId())
                .createdByName(user.getName())
                .createdByEmail(user.getEmail())
                .assignedToId(null)
                .assignedToName(null)
                .createdAt(savedTicket.getCreatedAt())
                .updatedAt(savedTicket.getUpdatedAt())
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Méthodes utilitaires privées
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Extrait le contenu du message à partir du JSON retourné par l'API Ollama.
     */
    private String extractMessageContent(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return "Bonjour, comment puis-je vous aider ?";
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            if (root.has("message") && root.get("message").has("content")) {
                return root.get("message").get("content").asText();
            }

            if (root.has("choices") && root.get("choices").isArray() && !root.get("choices").isEmpty()) {
                JsonNode messageNode = root.get("choices").get(0).get("message");
                if (messageNode != null && messageNode.has("content")) {
                    return messageNode.get("content").asText();
                }
            }

            if (root.has("reply")) {
                return rawResponse;
            }

        } catch (Exception e) {
            log.warn("Impossible de parser le wrapper JSON de la réponse LLM, utilisation en texte brut");
        }

        return rawResponse;
    }

    /**
     * Extrait la première paire JSON { ... } d'un texte brut.
     * Retourne null si aucun objet JSON n'est trouvé.
     */
    private String extractJsonObject(String text) {
        if (text == null || text.isBlank()) return null;

        // Nettoyer les balises Markdown éventuelles (```json ... ```)
        String cleaned = text.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();

        int firstBrace = cleaned.indexOf('{');
        int lastBrace  = cleaned.lastIndexOf('}');

        if (firstBrace != -1 && lastBrace > firstBrace) {
            return cleaned.substring(firstBrace, lastBrace + 1);
        }
        return null;
    }

    /**
     * Retourne la valeur texte d'un champ JSON, ou la valeur par défaut si absent/null/vide.
     */
    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            String value = node.get(field).asText().trim();
            return value.isBlank() ? defaultValue : value;
        }
        return defaultValue;
    }

    /**
     * Parse et nettoie le texte de réponse produit par le modèle LLM.
     */
    private ParsedAiContent parseContentText(String contentText) {
        ParsedAiContent result = new ParsedAiContent();

        if (contentText == null || contentText.isBlank()) {
            result.replyText = "Bonjour, comment puis-je vous aider aujourd'hui ?";
            return result;
        }

        String raw = contentText.trim();

        // Tenter un parsing d'un objet JSON unique si le modèle en produit un
        try {
            int firstBrace = raw.indexOf('{');
            int lastBrace = raw.lastIndexOf('}');
            if (firstBrace != -1 && lastBrace > firstBrace) {
                String jsonCandidate = raw.substring(firstBrace, lastBrace + 1);
                JsonNode node = objectMapper.readTree(jsonCandidate);
                if (node.has("reply") && !node.get("reply").isNull()) {
                    result.replyText = node.get("reply").asText();
                    if (node.has("create_ticket")) result.createTicketSuggested = node.get("create_ticket").asBoolean(false);
                    if (node.has("title") && !node.get("title").isNull()) result.suggestedTitle = node.get("title").asText();
                    if (node.has("description") && !node.get("description").isNull()) result.suggestedDescription = node.get("description").asText();
                    return result;
                }
            }
        } catch (Exception e) {
            log.debug("Traitement en mode texte brut.");
        }

        // Nettoyer tout résidu de balises ou clés JSON
        String cleaned = cleanJsonBoilerplate(raw);
        result.replyText = cleaned.trim();
        return result;
    }

    /**
     * Nettoie les artéfacts de clés JSON et balises Markdown.
     */
    private String cleanJsonBoilerplate(String text) {
        if (text == null) return "";

        String s = text;
        s = s.replaceAll("```json", "").replaceAll("```", "");
        s = s.replaceAll("\\[RESOLU\\]", "");
        s = s.replaceAll("\\[CREER_TICKET\\]", "");
        s = s.replaceAll("\\[VOTRE_CONFIRMATION_IMPORMALA\\]_?", "");
        s = s.replaceAll("\"resolved\"\\s*:\\s*(true|false),?", "");
        s = s.replaceAll("\"create_ticket\"\\s*:\\s*(true|false),?", "");
        s = s.replaceAll("\"title\"\\s*:\\s*(null|\"[^\"]*\"),?", "");
        s = s.replaceAll("\"description\"\\s*:\\s*(null|\"[^\"]*\"),?", "");
        s = s.replaceAll("\"reply\"\\s*:\\s*\"", "");
        s = s.replaceAll("^\\s*\\{\\s*", "");
        s = s.replaceAll("\\s*\\}\\s*$", "");

        return s;
    }

    /**
     * Vérifie si le MESSAGE DE L'UTILISATEUR indique explicitement que le problème est résolu.
     */
    private boolean isUserConfirmingResolution(String userMessage) {
        if (userMessage == null) return false;
        String low = userMessage.toLowerCase().trim();
        return low.contains("c'est résolu") || low.contains("cest resolu") ||
               low.contains("problème réglé") || low.contains("probleme regle") ||
               low.contains("ça marche") || low.contains("ca marche") ||
               low.contains("ça fonctionne") || low.contains("ca fonctionne") ||
               low.equals("résolu") || low.equals("resolu") ||
               low.contains("merci c'est bon") || low.contains("c'est bon merci");
    }

    /**
     * Vérifie si le message de l'utilisateur demande explicitement la création d'un ticket.
     */
    private boolean isUserRequestingTicket(String userMessage) {
        if (userMessage == null) return false;
        String low = userMessage.toLowerCase().trim();
        return low.contains("créer un ticket") || low.contains("creer un ticket") ||
               low.contains("ouvre un ticket") || low.contains("ouvrir un ticket");
    }

    private static class ParsedAiContent {
        String replyText;
        boolean resolved = false;
        boolean createTicketSuggested = false;
        String suggestedTitle;
        String suggestedDescription;
    }

    private String defaultResolvedTitle(String userMessage) {
        if (userMessage == null) {
            return "AI Résolution";
        }
        return "AI Résolution - " + (userMessage.length() > 50 ? userMessage.substring(0, 47) + "..." : userMessage);
    }
}

