package com.minidesk.dto.request;

import com.minidesk.enums.Priority;
import com.minidesk.enums.TicketCategory;
import com.minidesk.enums.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketRequest {

    @NotBlank(message = "Le titre du ticket est obligatoire")
    private String title;
    private String description;
    @NotNull(message = "La catégorie est obligatoire")
    private TicketCategory category;
    @NotNull(message = "La priorité est obligatoire")
    private Priority priority;
    private TicketStatus status;
    //Optional
    private Long assignedToId;
}
