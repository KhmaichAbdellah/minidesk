package com.minidesk.dto.response;

import com.minidesk.enums.TicketStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutLogResponse {

    private Long id;
    private Long ticketId;
    private TicketStatus oldStatus;
    private TicketStatus newStatus;
    private Long changedById;
    private String changedByName;
    private String changedByEmail;
    private LocalDateTime changedAt;
}
