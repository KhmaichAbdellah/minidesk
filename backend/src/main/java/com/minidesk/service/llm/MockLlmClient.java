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

        // Détecter si c'est un message de salutation
        if (prompt != null) {
            String lower = prompt.toLowerCase();
            if (lower.contains("bonjour") || lower.contains("salut") || lower.contains("hello") || lower.contains("bonsoir")) {
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
            if (lower.contains("merci") || lower.contains("thank")) {
                return buildChatResponse(
                    "Avec plaisir ! 😊 N'hésitez pas à revenir si vous avez d'autres questions.\n\n" +
                    "Si votre problème est résolu, vous pouvez le confirmer et je créerai automatiquement un ticket résolu pour la traçabilité.",
                    false, null, null
                );
            }
        }

        // Réponse générique pour tout autre message
        return buildChatResponse(
            "🤖 *Mode démonstration activé* — l'assistant IA MILO est en simulation.\n\n" +
            "En production, MILO analysera votre problème et vous guidera avec des étapes précises.\n\n" +
            "Pour activer le vrai assistant, configurez `LLM_PROVIDER=ollama` (local) ou `LLM_PROVIDER=openai` avec votre clé API.",
            false, null, null
        );
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
