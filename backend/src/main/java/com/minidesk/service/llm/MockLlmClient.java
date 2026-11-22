package com.minidesk.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implémentation simulée du client LLM.
 *
 * <p>Utilisé quand {@code llm.mock=true} (variable d'env {@code LLM_MOCK=true}).
 * Retourne des réponses JSON structurées statiques sans appeler aucun LLM réel.
 * Utile pour le développement local et les tests sans Ollama ni OpenAI.
 */
@Component
@Slf4j
public class MockLlmClient implements LlmClient {

    private static final String MOCK_ENDPOINT = "mock://localhost";
    private static final String MOCK_MODEL    = "mock-v1";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String sendPrompt(String prompt) {
        log.info("🤖 [MOCK LLM] sendPrompt appelé (mode simulation)");

        // Détecter si c'est un prompt de génération de ticket JSON
        if (prompt != null && prompt.contains("\"title\"") && prompt.contains("\"category\"")) {
            return buildTicketGenerationResponse();
        }

        // ── Extraire UNIQUEMENT le dernier message utilisateur ───────────────
        // Le prompt contient le marqueur "=== NOUVEAU MESSAGE DE L'UTILISATEUR ==="
        // suivi du message réel sur la ligne suivante.
        String userMessage = extractUserMessage(prompt);
        log.debug("🤖 [MOCK LLM] Message utilisateur extrait : '{}'", userMessage);

        if (userMessage != null) {
            String lower = userMessage.toLowerCase().trim();

            // 0. Problème Base de données / PostgreSQL / SQL (prioritaire)
            if (lower.matches(".*(postgre|postgres|postgresql|base de donn[ée]es|bdd|database|sql|psql|hibernate|pg_ctl).*")) {
                return buildChatResponse(
                    "Bonjour ! Problème de base de données PostgreSQL détecté. Voici les vérifications et manipulations à réaliser :\n\n" +
                    "**1. Vérifier si le serveur PostgreSQL est bien démarré**\n" +
                    "```powershell\n# Si vous utilisez Docker :\ndocker compose up -d postgres\n\n# Si vous utilisez le service local Windows :\nGet-Service *postgres*\nnet start postgresql-x64-18\n```\n\n" +
                    "**2. Tester l'écoute sur le port 5432**\n" +
                    "```powershell\nTest-NetConnection -ComputerName localhost -Port 5432\n```\n\n" +
                    "**3. Vérifier les identifiants dans `.env` ou `application.yaml`**\n" +
                    "- Hôte : `localhost` (ou `postgres` sous Docker)\n" +
                    "- Port : `5432`\n" +
                    "- Nom de la base : `minideskdb`\n" +
                    "- Utilisateur : `postgres`\n" +
                    "- Mot de passe : variable `DB_PASSWORD` (ou `SPRING_DATASOURCE_PASSWORD`)\n\n" +
                    "Voulez-vous que je crée un ticket de support incident pour le pôle Base de données / DevOps ?",
                    true, "Incident Base de données PostgreSQL / Connexion échouée", "L'utilisateur signale une erreur de connexion à la base de données PostgreSQL (port 5432)."
                );
            }

            // 1. Problème réseau / connexion internet (avec ou sans accents)
            if (lower.matches(".*(r[ée]seau|internet|wifi|wi-fi|ethernet|dns|ip|ping|vpn|proxy|d[ée]connect|connexion internet).*")) {
                return buildChatResponse(
                    "Bonjour ! Problème réseau détecté. Voici les diagnostics et manipulations à réaliser :\n\n" +
                    "**1. Vérifier la connectivité locale et passerelle**\n" +
                    "```powershell\nping 127.0.0.1\nping 8.8.8.8\ntracert google.com\n```\n\n" +
                    "**2. Réinitialiser la pile IP & vider le cache DNS**\n" +
                    "```powershell\nipconfig /release\nipconfig /renew\nipconfig /flushdns\n```\n\n" +
                    "**3. Vérifier les adaptateurs réseau**\n" +
                    "Assurez-vous que votre carte Wi-Fi ou Ethernet est bien activée dans `ncpa.cpl`.\n\n" +
                    "Voulez-vous que je crée un ticket de support incident pour le pôle Réseau ?",
                    true, "Incident Réseau / Perte de connectivité", "L'utilisateur signale un dysfonctionnement réseau ou une perte d'accès internet."
                );
            }

            // 2. Problème avec la voix / micro / audio
            if (lower.matches(".*(voix|micro|microphone|audio|son|casque|headset|speak|haut.parleur|volume).*")) {
                return buildChatResponse(
                    "Je comprends que vous avez un problème audio/microphone. Voici les étapes à vérifier :\n\n" +
                    "**1. Vérifier les paramètres Windows**\n" +
                    "```\nParamètres → Système → Son → Périphériques d'entrée\n```\n" +
                    "Assurez-vous que le bon micro est sélectionné et non muté.\n\n" +
                    "**2. Tester le microphone**\n" +
                    "```\nDémarrer → Paramètres → Système → Son → Tester votre microphone\n```\n\n" +
                    "**3. Vérifier les pilotes audio**\n" +
                    "```powershell\n# Dans le Gestionnaire de périphériques :\ndevmgmt.msc\n# → Contrôleurs audio, vidéo et jeux\n```\n\n" +
                    "**4. Vérifier les permissions de l'application**\n" +
                    "```\nParamètres → Confidentialité & sécurité → Microphone\n```\n\n" +
                    "Pouvez-vous me préciser :\n- Quel appareil utilisez-vous (casque USB, jack 3.5mm, webcam intégrée) ?\n- Le problème est-il dans une application spécifique (Teams, Zoom...) ou global ?",
                    false, null, null
                );
            }

            // 3. Problème mot de passe / accès / login
            if (lower.matches(".*(mot de passe|password|mdp|login|connexion|acc[èe]s|compte|verrouill[ée]|bloqu[ée]).*")) {
                return buildChatResponse(
                    "Problème d'accès ou de mot de passe. Voici les options :\n\n" +
                    "**1. Réinitialiser votre mot de passe** — Contactez votre administrateur système.\n\n" +
                    "**2. Compte verrouillé (Active Directory)**\n" +
                    "```powershell\n# Admin uniquement :\nUnlock-ADAccount -Identity \"username\"\n```\n\n" +
                    "**3. Pour MiniDesk spécifiquement** — Un administrateur peut réinitialiser votre mot de passe depuis la page Gestion des utilisateurs.\n\n" +
                    "Voulez-vous que je crée un ticket de support pour ce problème ?",
                    true, "Problème d'accès / mot de passe", "L'utilisateur rencontre un problème d'accès ou de mot de passe nécessitant l'intervention d'un administrateur."
                );
            }

            // 4. Imprimante / périphérique
            if (lower.matches(".*(imprimante|printer|scanner|imprimer|impression|peripherique|p[ée]riph[ée]rique|usb|bluetooth).*")) {
                return buildChatResponse(
                    "Problème avec un périphérique. Checklist rapide :\n\n" +
                    "**1. Vérifier la connexion physique**\n" +
                    "- Câble USB bien branché / Bluetooth activé\n" +
                    "- Imprimante allumée et en ligne\n\n" +
                    "**2. Vérifier la file d'attente**\n" +
                    "```\nDémarrer → Imprimantes et scanners → Ouvrir la file d'attente\n```\n\n" +
                    "**3. Redémarrer le spouleur d'impression**\n" +
                    "```powershell\nRestart-Service -Name Spooler\n```\n\n" +
                    "Quel est exactement le modèle de l'imprimante et le message d'erreur affiché ?",
                    false, null, null
                );
            }

            // 5. Remerciements / résolution
            if (lower.matches(".*(merci|thank|c'est bon|c'est r[ée]solu|ça marche|r[ée]solu|regl[ée]|parfait|nickel).*")) {
                return buildChatResponse(
                    "Avec plaisir ! 😊 N'hésitez pas à revenir si vous avez d'autres questions.\n\n" +
                    "Si votre problème est résolu, vous pouvez le confirmer et je créerai automatiquement un ticket résolu pour la traçabilité.",
                    false, null, null
                );
            }

            // 6. Salutations simples
            if (lower.matches(".*(bonjour|salut|hello|bonsoir|hey|coucou|bonne journ[ée]e).*")) {
                return buildChatResponse(
                    "Bonjour ! 👋 Je suis **MILO**, votre assistant IT MiniDesk. Comment puis-je vous aider aujourd'hui ?\n\n" +
                    "Je peux vous aider avec :\n" +
                    "- 🖥️ Des problèmes matériels ou logiciels\n" +
                    "- 🌐 Des questions réseau ou système\n" +
                    "- 🔧 Du support applicatif ou DevOps\n\n" +
                    "Décrivez votre problème et je ferai de mon mieux pour vous guider !",
                    false, null, null
                );
            }
        }

        // Réponse générique pour tout autre message
        return buildChatResponse(
            "🤖 *Mode démonstration activé — assistant IA simulé.*\n\n" +
            "J'ai bien reçu votre message. En production avec un vrai LLM (Ollama ou OpenAI), " +
            "MILO analyserait votre problème en détail et vous guiderait étape par étape.\n\n" +
            "Pour activer le vrai assistant :\n" +
            "- **Local** : `LLM_MOCK=false` + `LLM_PROVIDER=ollama` (nécessite Ollama)\n" +
            "- **Production** : `LLM_PROVIDER=openai` + votre clé `OPENAI_API_KEY`\n\n" +
            "Souhaitez-vous que je crée un ticket de support pour votre demande ?",
            true,
            "Demande d'assistance",
            "L'utilisateur a besoin d'aide (mode démonstration actif)."
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Extrait le message réel de l'utilisateur du prompt complet.
     * Le prompt contient le marqueur "=== NOUVEAU MESSAGE DE L'UTILISATEUR ===" suivi du message.
     */
    private String extractUserMessage(String prompt) {
        if (prompt == null) return null;
        final String MARKER = "=== NOUVEAU MESSAGE DE L'UTILISATEUR ===";
        int idx = prompt.lastIndexOf(MARKER);
        if (idx == -1) return prompt; // fallback : tout le prompt
        String afterMarker = prompt.substring(idx + MARKER.length()).trim();
        // Prendre uniquement la première ligne (le message utilisateur)
        String[] lines = afterMarker.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("===")) {
                return trimmed;
            }
        }
        return afterMarker.trim();
    }


    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public String getEndpoint() {
        return MOCK_ENDPOINT;
    }

    @Override
    public String getModel() {
        return MOCK_MODEL;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Retourne une réponse JSON simulée pour la génération de ticket.
     */
    private String buildTicketGenerationResponse() {
        try {
            Map<String, Object> contentMap = new LinkedHashMap<>();
            contentMap.put("title", "Demande d'assistance IT");
            contentMap.put("description", "Ticket généré automatiquement en mode démonstration.");
            contentMap.put("category", "OTHER");
            contentMap.put("priority", "NORMALE");

            String contentJson = objectMapper.writeValueAsString(contentMap);

            Map<String, Object> messageMap = new LinkedHashMap<>();
            messageMap.put("content", contentJson);

            Map<String, Object> rootMap = new LinkedHashMap<>();
            rootMap.put("message", messageMap);

            return objectMapper.writeValueAsString(rootMap);
        } catch (Exception e) {
            log.error("Erreur JSON ticket mock", e);
            return "{\"message\":{\"content\":\"{}\"}}";
        }
    }

    /**
     * Retourne une réponse JSON au format Ollama simulé pour le chat.
     */
    private String buildChatResponse(String reply, boolean createTicket, String title, String description) {
        try {
            Map<String, Object> contentMap = new LinkedHashMap<>();
            contentMap.put("reply", reply);
            contentMap.put("create_ticket", createTicket);
            contentMap.put("title", title);
            contentMap.put("description", description);

            String contentJson = objectMapper.writeValueAsString(contentMap);

            Map<String, Object> messageMap = new LinkedHashMap<>();
            messageMap.put("role", "assistant");
            messageMap.put("content", contentJson);

            Map<String, Object> rootMap = new LinkedHashMap<>();
            rootMap.put("message", messageMap);
            rootMap.put("done", true);

            return objectMapper.writeValueAsString(rootMap);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du JSON mock", e);
            return "{\"message\":{\"role\":\"assistant\",\"content\":\"{\\\"reply\\\":\\\"Bonjour ! Comment puis-je vous aider ?\\\"}\"},\"done\":true}";
        }
    }
}
