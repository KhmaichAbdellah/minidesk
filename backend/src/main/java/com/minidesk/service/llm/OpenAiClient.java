package com.minidesk.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client LLM utilisant l'API OpenAI (GPT-4o, GPT-4o-mini, etc.)
 * Sélectionné par LlmClientConfig quand llm.provider=openai dans application.yaml
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiClient implements LlmClient {

    private final RestTemplate restTemplate;
    private final OpenAiProperties openAiProperties;
    private final LlmProperties llmProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Endpoint officiel de l'API OpenAI
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Override
    public String sendPrompt(String prompt) {

        // ── Construction du corps de la requête (format OpenAI Chat Completions) ──
        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiProperties.getModel());
        body.put("max_tokens", llmProperties.getMaxNewTokens());
        body.put("temperature", llmProperties.getTemperature());
        body.put("top_p", llmProperties.getTopP());
        body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        // ── Headers avec clé API ──────────────────────────────────────────────────
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getApiKey());  // Authorization: Bearer sk-...

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("===== APPEL OPENAI =====");
            log.info("Model : {}", openAiProperties.getModel());
            log.info("Max tokens : {}", llmProperties.getMaxNewTokens());

            ResponseEntity<String> response = restTemplate.postForEntity(
                    OPENAI_API_URL,
                    entity,
                    String.class
            );

            String rawBody = response.getBody();
            log.info("===== REPONSE OPENAI =====");

            // ── Convertir la réponse OpenAI en format attendu par AiService ─────────
            // AiService attend un JSON avec le champ "message.content" (format Ollama)
            // On réutilise le même format pour ne pas toucher AiService.
            return convertOpenAiResponseToOllamaFormat(rawBody);

        } catch (Exception ex) {
            log.error("Erreur OpenAI API : {}", ex.getMessage());

            if (llmProperties.isMock()) {
                return buildMockResponse();
            }

            throw new RuntimeException(
                    "Impossible d'appeler l'API OpenAI : " + ex.getMessage()
                            + " | Vérifiez votre clé API (OPENAI_API_KEY) et votre quota.",
                    ex
            );
        }
    }

    /**
     * Convertit la réponse OpenAI au format attendu par AiService.
     *
     * Réponse OpenAI :
     * {
     *   "choices": [{ "message": { "role": "assistant", "content": "..." } }]
     * }
     *
     * Format attendu par AiService (format Ollama) :
     * {
     *   "message": { "content": "..." }
     * }
     */
    private String convertOpenAiResponseToOllamaFormat(String openAiJson) {
        try {
            JsonNode root = objectMapper.readTree(openAiJson);
            String content = root
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");

            // Reconstruire dans le format qu'attend AiService (même format qu'Ollama)
            Map<String, Object> ollamaFormat = new HashMap<>();
            ollamaFormat.put("message", Map.of("content", content));

            return objectMapper.writeValueAsString(ollamaFormat);

        } catch (Exception e) {
            log.error("Impossible de parser la réponse OpenAI", e);
            throw new RuntimeException("Réponse OpenAI invalide", e);
        }
    }

    private String buildMockResponse() {
        try {
            Map<String, Object> mock = new HashMap<>();
            mock.put("message", Map.of("content",
                    "{\"reply\":\"Réponse simulée (OpenAI indisponible)\",\"resolved\":false,\"create_ticket\":true,\"title\":\"Ticket IA\",\"description\":\"Simulation\"}"
            ));
            return objectMapper.writeValueAsString(mock);
        } catch (JsonProcessingException e) {
            return "{\"message\":{\"content\":\"{\\\"reply\\\":\\\"Simulation\\\",\\\"resolved\\\":false}\"}}";
        }
    }

    @Override public String getName()     { return "openai"; }
    @Override public String getEndpoint() { return OPENAI_API_URL; }
    @Override public String getModel()    { return openAiProperties.getModel(); }
}
