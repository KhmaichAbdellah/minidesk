package com.minidesk.config;

import com.minidesk.service.llm.LlmClient;
import com.minidesk.service.llm.LlmProperties;
import com.minidesk.service.llm.OllamaClient;
import com.minidesk.service.llm.OpenAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du client LLM.
 * Sélectionne automatiquement OllamaClient ou OpenAiClient
 * selon la valeur de llm.provider dans application.yaml.
 *
 * ┌─────────────────┬────────────────────────────────────┐
 * │ llm.provider    │ Client injecté                     │
 * ├─────────────────┼────────────────────────────────────┤
 * │ ollama (défaut) │ OllamaClient (LLM local)           │
 * │ openai          │ OpenAiClient (API GPT-4o)          │
 * └─────────────────┴────────────────────────────────────┘
 */
@Configuration
@Slf4j
public class LlmClientConfig {

    @Bean
    public LlmClient llmClient(
            LlmProperties llmProperties,
            OllamaClient ollamaClient,
            OpenAiClient openAiClient
    ) {
        String provider = llmProperties.getProvider();

        switch (provider.toLowerCase()) {
            case "openai" -> {
                log.info("🤖 LLM Provider : OpenAI (model={})", openAiClient.getModel());
                return openAiClient;
            }
            case "ollama" -> {
                log.info("🤖 LLM Provider : Ollama (endpoint={})", ollamaClient.getEndpoint());
                return ollamaClient;
            }
            default -> {
                log.warn("⚠️  LLM Provider inconnu '{}' — fallback sur Ollama", provider);
                return ollamaClient;
            }
        }
    }
}