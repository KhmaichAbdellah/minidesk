package com.minidesk.service.impl;

import com.minidesk.dto.request.KnowledgeArticleRequest;
import com.minidesk.dto.response.KnowledgeArticleResponse;
import com.minidesk.entity.KnowledgeArticle;
import com.minidesk.entity.User;
import com.minidesk.enums.TicketCategory;
import com.minidesk.exception.ResourceNotFoundException;
import com.minidesk.repository.KnowledgeArticleRepository;
import com.minidesk.service.KnowledgeArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeArticleServiceImpl implements KnowledgeArticleService {

    private final KnowledgeArticleRepository articleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeArticleResponse> getAllArticles(String search, TicketCategory category) {
        List<KnowledgeArticle> articles;

        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasCategory = category != null;

        if (hasSearch && hasCategory) {
            String query = search.trim();
            articles = articleRepository.findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndContentContainingIgnoreCase(
                    category, query, category, query);
        } else if (hasSearch) {
            String query = search.trim();
            articles = articleRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query);
        } else if (hasCategory) {
            articles = articleRepository.findByCategory(category);
        } else {
            articles = articleRepository.findAll();
        }

        return articles.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeArticleResponse getArticleById(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable avec l'id : " + id));
        return mapToResponse(article);
    }

    @Override
    @Transactional
    public KnowledgeArticleResponse createArticle(KnowledgeArticleRequest request, User author) {
        KnowledgeArticle article = KnowledgeArticle.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .category(request.getCategory())
                .keywords(request.getKeywords() != null ? new ArrayList<>(request.getKeywords()) : new ArrayList<>())
                .author(author)
                .build();

        KnowledgeArticle saved = articleRepository.save(article);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public KnowledgeArticleResponse updateArticle(Long id, KnowledgeArticleRequest request, User currentUser) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable avec l'id : " + id));

        article.setTitle(request.getTitle().trim());
        article.setContent(request.getContent().trim());
        article.setCategory(request.getCategory());
        if (request.getKeywords() != null) {
            article.setKeywords(new ArrayList<>(request.getKeywords()));
        }

        KnowledgeArticle updated = articleRepository.save(article);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id, User currentUser) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article introuvable avec l'id : " + id);
        }
        articleRepository.deleteById(id);
    }

    private KnowledgeArticleResponse mapToResponse(KnowledgeArticle article) {
        return KnowledgeArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .category(article.getCategory())
                .keywords(article.getKeywords())
                .authorId(article.getAuthor() != null ? article.getAuthor().getId() : null)
                .authorName(article.getAuthor() != null ? article.getAuthor().getName() : "Auteur inconnu")
                .authorEmail(article.getAuthor() != null ? article.getAuthor().getEmail() : null)
                .createdAt(article.getCreatedAt())
                .build();
    }
}
