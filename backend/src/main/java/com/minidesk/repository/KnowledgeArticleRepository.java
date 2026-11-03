package com.minidesk.repository;

import com.minidesk.entity.KnowledgeArticle;
import com.minidesk.enums.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByCategory(TicketCategory category);

    List<KnowledgeArticle> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content);

    List<KnowledgeArticle> findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndContentContainingIgnoreCase(
            TicketCategory category1, String title, TicketCategory category2, String content);
}
