package com.minidesk.service.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

            // Salutations
            if (lower.matches(".*(bonjour|salut|hello|bonsoir|hey|coucou|bonne journée).*")) {
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

            // Remerciements / résolution
            if (lower.matches(".*(merci|thank|c'est bon|c'est résolu|ça marche|résolu|regle|parfait|nickel).*")) {
                return buildChatResponse(
                    "Avec plaisir ! 😊 N'hésitez pas à revenir si vous avez d'autres questions.\n\n" +
                    "Si votre problème est résolu, vous pouvez le confirmer et je créerai automatiquement un ticket résolu pour la traçabilité.",
                    false, null, null
                );
            }

            // Problème avec la voix / micro / audio
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

            // Problème réseau / connexion
            if (lower.matches(".*(réseau|internet|connexion|wifi|ethernet|dns|ip|ping|vpn|proxy|déconnect).*")) {
                return buildChatResponse(
                    "Problème réseau détecté. Voici les diagnostics de base :\n\n" +
                    "**1. Vérifier la connectivité**\n" +
                    "```powershell\nping 8.8.8.8\ntracert google.com\n```\n\n" +
                    "**2. Renouveler le bail DHCP**\n" +
                    "```powershell\nipconfig /release\nipconfig /renew\nipconfig /flushdns\n```\n\n" +
                    "Décrivez plus précisément votre problème pour une aide ciblée.",
                    false, null, null
                );
            }

            // Problème mot de passe / accès / login
            if (lower.matches(".*(mot de passe|password|mdp|login|connexion|accès|compte|verrouillé|bloqué).*")) {
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

            // Imprimante / périphérique
            if (lower.matches(".*(imprimante|printer|scanner|imprimer|impression|peripherique|périphérique|usb|bluetooth).*")) {
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
        return """
                {
                  "message": {
                    "content": "{\\"title\\":\\"Demande d'assistance IT\\",\\"description\\":\\"Ticket généré automatiquement en mode démonstration.\\",\\"category\\":\\"OTHER\\",\\"priority\\":\\"NORMALE\\"}"
                  }
                }
                """;
    }

    /**
     * Retourne une réponse JSON au format Ollama simulé pour le chat.
     */
    private String buildChatResponse(String reply, boolean createTicket, String title, String description) {
        // Échapper les guillemets dans la réponse
        String escapedReply = reply
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n");

        return String.format("""
                {
                  "message": {
                    "role": "assistant",
                    "content": "{\\\"reply\\\":\\\"%s\\\",\\\"create_ticket\\\":%s,\\\"title\\\":%s,\\\"description\\\":%s}"
                  },
                  "done": true
                }
                """,
            escapedReply,
            createTicket,
            title != null ? "\"" + title + "\"" : "null",
            description != null ? "\"" + description + "\"" : "null"
        );
    }
}
