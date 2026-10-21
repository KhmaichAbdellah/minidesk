package com.minidesk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {

    private String reply;
    private Long sessionId;
    private Boolean resolvedByAi;
    private Boolean suggestedCreateTicket;
    private String suggestedTicketTitle;
    private String suggestedTicketDescription;
}
