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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User author;
    private User otherUser;
    private User admin;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        author = User.builder().id(1L).name("Julie").email("julie@minidesk.com").role(Role.USER).build();
        otherUser = User.builder().id(2L).name("Thomas").email("thomas@minidesk.com").role(Role.USER).build();
        admin = User.builder().id(99L).name("Admin").email("admin@minidesk.com").role(Role.ADMIN).build();
        ticket = Ticket.builder().id(10L).title("Ticket test").createdBy(author).build();
    }

    @Test
    void addComment_Success_ByAuthor() {
        CommentRequest req = CommentRequest.builder().content("Mon premier commentaire").build();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(100L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });

        CommentResponse res = commentService.addComment(10L, req, author);

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals("Mon premier commentaire", res.getContent());
        assertEquals(1L, res.getAuthorId());
        assertEquals("Julie", res.getAuthorName());
    }

    @Test
    void addComment_Denied_ByOtherUser() {
        CommentRequest req = CommentRequest.builder().content("Tentative pirate").build();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(AccessDeniedException.class, () -> commentService.addComment(10L, req, otherUser));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_Success_ByAdmin() {
        CommentRequest req = CommentRequest.builder().content("Message du support").build();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(101L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });

        CommentResponse res = commentService.addComment(10L, req, admin);

        assertNotNull(res);
        assertEquals(99L, res.getAuthorId());
        assertEquals("Admin", res.getAuthorName());
    }

    @Test
    void addComment_NotFound_Ticket() {
        CommentRequest req = CommentRequest.builder().content("Test").build();
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.addComment(999L, req, author));
    }

    @Test
    void getCommentsByTicket_Success() {
        Comment c = Comment.builder().id(1L).ticket(ticket).author(author).content("Historique").createdAt(LocalDateTime.now()).build();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(c));

        List<CommentResponse> list = commentService.getCommentsByTicket(10L, author);

        assertEquals(1, list.size());
        assertEquals("Historique", list.get(0).getContent());
    }
}
