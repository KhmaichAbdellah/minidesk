package com.minidesk.dto.response;

import com.minidesk.enums.TicketCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeArticleResponse {

    private Long id;
    private String title;
    private String content;
    private TicketCategory category;
    private List<String> keywords;
    private Long authorId;
    private String authorName;
    private String authorEmail;
    private LocalDateTime createdAt;
}
