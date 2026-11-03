package com.minidesk.service.impl;

import com.minidesk.dto.request.CommentRequest;
import com.minidesk.dto.response.CommentResponse;
import com.minidesk.entity.Comment;
import com.minidesk.entity.Ticket;
import com.minidesk.entity.User;
import com.minidesk.enums.Role;
import com.minidesk.exception.ResourceNotFoundException;
import com.minidesk.repository.CommentRepository;
import com.minidesk.repository.TicketRepository;
import com.minidesk.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public CommentResponse addComment(Long ticketId, CommentRequest request, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable avec l'id : " + ticketId));

        validateAccess(ticket, currentUser);

        Comment comment = Comment.builder()
                .ticket(ticket)
                .author(currentUser)
                .content(request.getContent().trim())
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicket(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable avec l'id : " + ticketId));

        validateAccess(ticket, currentUser);

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateAccess(Ticket ticket, User currentUser) {
        if (currentUser.getRole() == Role.USER) {
            boolean isAuthor = ticket.getCreatedBy() != null && ticket.getCreatedBy().getId().equals(currentUser.getId());
            if (!isAuthor) {
                throw new AccessDeniedException("Accès refusé : vous n'êtes pas l'auteur de ce ticket.");
            }
        }
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : "Utilisateur inconnu")
                .authorEmail(comment.getAuthor() != null ? comment.getAuthor().getEmail() : null)
                .authorRole(comment.getAuthor() != null && comment.getAuthor().getRole() != null ? comment.getAuthor().getRole().name() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
