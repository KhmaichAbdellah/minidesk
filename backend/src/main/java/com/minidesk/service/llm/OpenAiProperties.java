package com.minidesk.service.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriétés de configuration pour l'API OpenAI.
 * Lues depuis application.yaml sous le préfixe "openai".
 *
 * ⚠️ La clé API (apiKey) doit TOUJOURS être injectée via variable d'environnement :
 *    OPENAI_API_KEY=sk-...
 *    Ne jamais l'écrire en dur dans application.yaml !
 */
@Data
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    /**
     * Clé API OpenAI — injectée via ${OPENAI_API_KEY}
     * Obtenir sur : https://platform.openai.com/api-keys
     */
    private String apiKey = "";

    /**
     * Modèle OpenAI à utiliser.
     * Options recommandées :
     *   - gpt-4o-mini  → Rapide et pas cher (~$0.15/1M tokens input)
     *   - gpt-4o       → Plus puissant (~$5/1M tokens input)
     *   - gpt-3.5-turbo → Le moins cher (~$0.50/1M tokens input)
     */
    private String model = "gpt-4o-mini";
}
