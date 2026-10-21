package com.minidesk.service.impl;

import com.minidesk.dto.request.KnowledgeArticleRequest;
import com.minidesk.dto.response.KnowledgeArticleResponse;
import com.minidesk.entity.KnowledgeArticle;
import com.minidesk.entity.User;
import com.minidesk.enums.TicketCategory;
import com.minidesk.exception.ResourceNotFoundException;
import com.minidesk.repository.KnowledgeArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleServiceImplTest {

    @Mock
    private KnowledgeArticleRepository articleRepository;

    @InjectMocks
    private KnowledgeArticleServiceImpl articleService;

    private User author;
    private KnowledgeArticle article;

    @BeforeEach
    void setUp() {
        author = User.builder().id(1L).name("Admin").email("admin@minidesk.com").build();
        article = KnowledgeArticle.builder()
                .id(10L)
                .title("Comment configurer le VPN")
                .content("Étapes pour le VPN")
                .category(TicketCategory.NETWORK)
                .keywords(List.of("vpn", "réseau"))
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllArticles_WithoutFilter() {
        when(articleRepository.findAll()).thenReturn(List.of(article));

        List<KnowledgeArticleResponse> res = articleService.getAllArticles(null, null);

        assertEquals(1, res.size());
        assertEquals("Comment configurer le VPN", res.get(0).getTitle());
    }

    @Test
    void getArticleById_Success() {
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));

        KnowledgeArticleResponse res = articleService.getArticleById(10L);

        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals(TicketCategory.NETWORK, res.getCategory());
    }

    @Test
    void getArticleById_NotFound() {
        when(articleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> articleService.getArticleById(999L));
    }

    @Test
    void createArticle_Success() {
        KnowledgeArticleRequest req = KnowledgeArticleRequest.builder()
                .title("Nouveau guide")
                .content("Contenu du guide")
                .category(TicketCategory.HARDWARE)
                .keywords(List.of("hardware"))
                .build();

        when(articleRepository.save(any(KnowledgeArticle.class))).thenAnswer(inv -> {
            KnowledgeArticle a = inv.getArgument(0);
            a.setId(20L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        KnowledgeArticleResponse res = articleService.createArticle(req, author);

        assertNotNull(res);
        assertEquals(20L, res.getId());
        assertEquals("Nouveau guide", res.getTitle());
        assertEquals(TicketCategory.HARDWARE, res.getCategory());
    }

    @Test
    void deleteArticle_Success() {
        when(articleRepository.existsById(10L)).thenReturn(true);

        articleService.deleteArticle(10L, author);

        verify(articleRepository, times(1)).deleteById(10L);
    }
}
