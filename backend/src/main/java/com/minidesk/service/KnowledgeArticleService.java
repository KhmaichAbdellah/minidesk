package com.minidesk.service;

import com.minidesk.dto.request.KnowledgeArticleRequest;
import com.minidesk.dto.response.KnowledgeArticleResponse;
import com.minidesk.entity.User;
import com.minidesk.enums.TicketCategory;

import java.util.List;

public interface KnowledgeArticleService {

    List<KnowledgeArticleResponse> getAllArticles(String search, TicketCategory category);

    KnowledgeArticleResponse getArticleById(Long id);

    KnowledgeArticleResponse createArticle(KnowledgeArticleRequest request, User author);

    KnowledgeArticleResponse updateArticle(Long id, KnowledgeArticleRequest request, User currentUser);

    void deleteArticle(Long id, User currentUser);
}
