package com.minidesk.config;

import com.minidesk.service.llm.LlmClient;
import com.minidesk.service.llm.LlmProperties;
import com.minidesk.service.llm.MockLlmClient;
import com.minidesk.service.llm.OllamaClient;
import com.minidesk.service.llm.OpenAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du client LLM.
 * Sélectionne automatiquement le bon client selon llm.provider et llm.mock.
 *
 * ┌──────────────────────┬────────────────────────────────────┐
 * │ llm.mock / provider  │ Client injecté                     │
 * ├──────────────────────┼────────────────────────────────────┤
 * │ mock=true            │ MockLlmClient (simulation locale)  │
 * │ provider=ollama      │ OllamaClient (LLM local)           │
 * │ provider=openai      │ OpenAiClient (API GPT-4o)          │
 * └──────────────────────┴────────────────────────────────────┘
 */
@Configuration
@Slf4j
public class LlmClientConfig {

    @Bean
    public LlmClient llmClient(
            LlmProperties llmProperties,
            MockLlmClient mockLlmClient,
            OllamaClient ollamaClient,
            OpenAiClient openAiClient
    ) {
        // ── Mode simulation : aucun LLM réel requis ──────────────────────────
        if (llmProperties.isMock()) {
            log.info("🤖 LLM Provider : Mock (simulation — LLM_MOCK=true)");
            return mockLlmClient;
        }

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