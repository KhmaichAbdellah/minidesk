package com.minidesk.service.llm;

import com.minidesk.dto.request.AiRequest;
import com.minidesk.entity.AiSession;
import com.minidesk.entity.Ticket;
import com.minidesk.entity.User;
import com.minidesk.enums.Priority;
import com.minidesk.enums.Role;
import com.minidesk.enums.TicketCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {


    private PromptBuilder promptBuilder;

    // Données de test réutilisées
    private User testUser;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();

        testUser = User.builder()
                .id(1L)
                .name("Abdellah Dev")
                .email("dev@minidesk.com")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    // ── Test 1 : Prompt simple sans ticket ni session ─────────────────────────
    @Test
    @DisplayName("build : sans ticket ni session → contient l'utilisateur et le message")
    void build_sansTiketNiSession_contientUtilisateurEtMessage() {
        AiRequest request = new AiRequest();
        request.setMessage("J'ai un problème avec le port 8080");

        String prompt = promptBuilder.build(testUser, request, null, null);

        // Vérifier que le prompt contient le nom de l'utilisateur
        assertThat(prompt).contains("Abdellah Dev");
        // Vérifier que le prompt contient le message de l'utilisateur
        assertThat(prompt).contains("J'ai un problème avec le port 8080");
        // Vérifier que le prompt contient les consignes de l'assistant
        assertThat(prompt).contains("assistant virtuel");
        assertThat(prompt).contains("français");
    }

    // ── Test 2 : Prompt avec ticket lié ───────────────────────────────────────
    @Test
    @DisplayName("build : avec un ticket lié → contient le contexte du ticket")
    void build_avecTicket_contientContexteTicket() {
        AiRequest request = new AiRequest();
        request.setMessage("Le problème persiste");

        Ticket ticket = Ticket.builder()
                .id(42L)
                .title("Erreur de connexion PostgreSQL")
                .description("Cannot connect to database on port 5432")
                .priority(Priority.HAUTE)
                .category(TicketCategory.SOFTWARE)
                .build();

        String prompt = promptBuilder.build(testUser, request, ticket, null);

        // Le contexte du ticket doit apparaître dans le prompt
        assertThat(prompt).contains("42");
        assertThat(prompt).contains("Erreur de connexion PostgreSQL");
        assertThat(prompt).contains("Cannot connect to database on port 5432");
    }

    // ── Test 3 : Prompt sans message utilisateur ──────────────────────────────
    @Test
    @DisplayName("build : message vide → prompt valide sans NPE")
    void build_messageVide_promptValide() {
        AiRequest request = new AiRequest();
        request.setMessage(""); // message vide

        // Ne doit pas lever de NullPointerException
        String prompt = promptBuilder.build(testUser, request, null, null);

        assertThat(prompt).isNotNull();
        assertThat(prompt).isNotBlank();
    }

    // ── Test 4 : Prompt contient les balises attendues ────────────────────────
    @Test
    @DisplayName("build : les consignes [RESOLU] et [CREER_TICKET] sont mentionnées")
    void build_contientBalisesConsignes() {
        AiRequest request = new AiRequest();
        request.setMessage("Test balises");

        String prompt = promptBuilder.build(testUser, request, null, null);

        assertThat(prompt).contains("[RESOLU]");
        assertThat(prompt).contains("[CREER_TICKET]");
    }
}
