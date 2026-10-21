package com.minidesk.dto.response;

import com.minidesk.enums.Priority;
import com.minidesk.enums.TicketCategory;


public record TicketGenerationResult(
        String title,
        String description,
        TicketCategory category,
        Priority priority
) {}
