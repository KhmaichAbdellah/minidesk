package com.minidesk.controller;

import com.minidesk.dto.request.KnowledgeArticleRequest;
import com.minidesk.dto.response.KnowledgeArticleResponse;
import com.minidesk.entity.User;
import com.minidesk.enums.TicketCategory;
import com.minidesk.service.KnowledgeArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "📚 Base de Connaissances (KB)", description = "Consultation et gestion des articles d'aide et solutions fréquentes")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class KnowledgeArticleController {

    private final KnowledgeArticleService articleService;

    @Operation(summary = "Rechercher et lister les articles",
        description = "Retourne la liste des articles de la base de connaissances avec filtres optionnels par mot-clé et catégorie.")
    @GetMapping
    public ResponseEntity<List<KnowledgeArticleResponse>> getAllArticles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketCategory category) {
        return ResponseEntity.ok(articleService.getAllArticles(search, category));
    }

    @Operation(summary = "Obtenir un article par ID")
    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeArticleResponse> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @Operation(summary = "Créer un nouvel article",
        description = "Accessible uniquement aux rôles ADMIN et TECHNICIAN.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<KnowledgeArticleResponse> createArticle(
            @Valid @RequestBody KnowledgeArticleRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleService.createArticle(request, currentUser));
    }

    @Operation(summary = "Modifier un article existant",
        description = "Accessible uniquement aux rôles ADMIN et TECHNICIAN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<KnowledgeArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeArticleRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(articleService.updateArticle(id, request, currentUser));
    }

    @Operation(summary = "Supprimer un article",
        description = "Accessible uniquement aux administrateurs.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        articleService.deleteArticle(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
