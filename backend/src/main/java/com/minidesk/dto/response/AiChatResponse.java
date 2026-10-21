package com.minidesk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    /**
     * Réponse textuelle de l'assistant IA.
     * Correspond au champ "reply" retourné par le modèle LLM.
     */
    private String reply;
}
