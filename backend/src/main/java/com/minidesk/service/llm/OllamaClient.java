package com.minidesk.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
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
 * Client LLM local Ollama.
 * Toujours instancié comme bean — LlmClientConfig décide lequel utiliser activement.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaClient implements LlmClient {

    private final RestTemplate restTemplate;
    private final OllamaProperties ollamaProperties;
    private final LlmProperties llmProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String sendPrompt(String prompt) {

        String endpoint = ollamaProperties.getEndpoint();

        Map<String, Object> body = new HashMap<>();
        body.put("model", ollamaProperties.getModel());

        // IMPORTANT
        body.put("stream", false);

        body.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", prompt
                )
        ));

        body.put("temperature", llmProperties.getTemperature());
        body.put("top_p", llmProperties.getTopP());
        body.put("max_tokens", llmProperties.getMaxNewTokens());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {

            log.info("===== APPEL OLLAMA =====");
            log.info("Endpoint : {}", endpoint);
            log.info("Model : {}", ollamaProperties.getModel());

            log.info("Endpoint utilisé = {}", endpoint);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            endpoint,
                            entity,
                            String.class
                    );

            log.info("===== REPONSE OLLAMA =====");
            log.info(response.getBody());

            return response.getBody();

        } catch (Exception ex) {

            log.error("Erreur Ollama", ex);

            if (llmProperties.isMock()) {
                return buildMockResponse(prompt);
            }

            throw new RuntimeException(
                    "Impossible d'appeler Ollama : " + ex.getMessage(),
                    ex
            );
        }
    }

    private String buildMockResponse(String prompt) {

        Map<String, Object> mock = new HashMap<>();

        mock.put("reply",
                "Réponse simulée : impossible de joindre Ollama.");

        mock.put("resolved", false);
        mock.put("create_ticket", true);
        mock.put("title", "Ticket proposé par l'IA");
        mock.put("description", prompt);

        try {
            return objectMapper.writeValueAsString(mock);
        } catch (JsonProcessingException e) {
            return """
                    {
                      "reply":"Réponse simulée",
                      "resolved":false,
                      "create_ticket":true
                    }
                    """;
        }
    }

    @Override
    public String getName() {
        return "ollama";
    }

    @Override
    public String getEndpoint() {
        return ollamaProperties.getEndpoint();
    }

    @Override
    public String getModel() {
        return ollamaProperties.getModel();
    }
}