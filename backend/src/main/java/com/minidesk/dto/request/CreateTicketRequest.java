package com.minidesk.dto.request;

import lombok.Data;
import com.minidesk.enums.Priority;
import com.minidesk.enums.TicketCategory;


@Data
public class CreateTicketRequest {

    private String title;
    private String description;
    private TicketCategory category = TicketCategory.OTHER;
    private Priority priority = Priority.NORMALE;
}
