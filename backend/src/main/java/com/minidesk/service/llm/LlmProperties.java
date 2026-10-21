package com.minidesk.service.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriétés de configuration générales du moteur LLM (lues depuis application.yaml sous le préfixe "llm").
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {
    /** Nom du fournisseur (défaut: "ollama"). */
    private String provider = "ollama";

    /** Mode simulation si le LLM n'est pas joignable (défaut: false). */
    private boolean mock = false;

    /** Nombre maximum de nouveaux tokens à générer. */
    private int maxNewTokens = 256;

    /** Degré de créativité de l'IA (0.0 = déterministe, 1.0 = créatif). */
    private double temperature = 1.0;

    /** Échantillonnage nucleus top-P. */
    private double topP = 1.0;

    /** Nombre maximum de candidats top-K. */
    private int topK = 50;
}
