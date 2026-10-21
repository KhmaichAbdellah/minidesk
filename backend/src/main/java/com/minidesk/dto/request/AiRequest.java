package com.minidesk.dto.request;

import lombok.Data;


@Data
public class AiRequest {

    private Long ticketId;
    private Long sessionId;
    private String message;
}
