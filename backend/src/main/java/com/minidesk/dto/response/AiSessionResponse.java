package com.minidesk.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


//  DTO retourné par l'API pour consulter l'historique d'une session de chat IA.

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSessionResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long ticketId;
    private String ticketTitle;
    private JsonNode messages;
    private Boolean resolvedByAi;
    private LocalDateTime createdAt;
}
