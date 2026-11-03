package com.minidesk.service.llm;

/**
 * Interface client pour interagir avec un modèle de langage (LLM).
 */
public interface LlmClient {
    /** Envoie le prompt au LLM et retourne sa réponse brute. */
    String sendPrompt(String prompt);

    /** Nom du fournisseur (ex: "ollama"). */
    String getName();

    /** URL de l'endpoint du LLM. */
    String getEndpoint();

    /** Nom du modèle utilisé (ex: "phi3:mini"). */
    String getModel();
}
