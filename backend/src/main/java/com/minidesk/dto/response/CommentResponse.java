package com.minidesk.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorName;
    private String authorEmail;
    private String authorRole;
    private String content;
    private LocalDateTime createdAt;
}
