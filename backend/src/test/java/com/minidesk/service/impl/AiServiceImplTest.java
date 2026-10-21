package com.minidesk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minidesk.dto.request.AiRequest;
import com.minidesk.dto.request.CreateTicketRequest;
import com.minidesk.dto.response.AiResponse;
import com.minidesk.dto.response.TicketGenerationResult;
import com.minidesk.dto.response.TicketResponse;
import com.minidesk.entity.AiSession;
import com.minidesk.entity.Ticket;
import com.minidesk.entity.User;
import com.minidesk.enums.Priority;
import com.minidesk.enums.Role;
import com.minidesk.enums.TicketCategory;
import com.minidesk.repository.AiSessionRepository;
import com.minidesk.repository.TicketRepository;
import com.minidesk.service.llm.LlmClient;
import com.minidesk.service.llm.LlmProperties;
import com.minidesk.service.llm.PromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link AiServiceImpl}.
 *
 * Vérifie :
 *   - La réponse de l'IA est bien extraite de la réponse JSON d'Ollama
 *   - La résolution n'est déclenchée QUE si l'utilisateur le confirme
 *   - En cas d'erreur LLM, une réponse de secours est renvoyée (pas de plantage)
 *   - Le ticket est bien créé automatiquement quand l'utilisateur dit "c'est résolu"
 *   - La génération automatique de ticket basée sur TOUTE la conversation
 *   - La conversion des enums category et priority
 *   - Les fallbacks en cas de réponses invalides ou session vide
 */
@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock private AiSessionRepository aiSessionRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private PromptBuilder promptBuilder;
    @Mock private LlmClient llmClient;
    @Mock private LlmProperties llmProperties;

    @InjectMocks
    private AiServiceImpl aiService;

    private User testUser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Dev Test")
                .email("dev@minidesk.com")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    private void mockSessionSave() {
        when(aiSessionRepository.save(any(AiSession.class))).thenAnswer(invocation -> {
            AiSession s = invocation.getArgument(0);
            s.setId(99L);
            return s;
        });
    }

    // ── Test 1 : Réponse normale d'Ollama (format /api/chat) ──────────────────
    @Test
    @DisplayName("chat : réponse Ollama format message.content → reply correctement extrait")
    void chat_reponseOllamaValide_extractionCorrecte() {
        String ollamaJson = """
            {
              "model": "phi3:mini",
              "message": {
                "role": "assistant",
                "content": "Voici la solution pour votre problème de port 8080."
              },
              "done": true
            }
            """;

        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("prompt test");
        when(llmClient.sendPrompt(anyString())).thenReturn(ollamaJson);
        mockSessionSave();

        AiRequest request = new AiRequest();
        request.setMessage("J'ai un problème avec le port 8080");

        AiResponse response = aiService.chat(testUser, request);

        assertThat(response).isNotNull();
        assertThat(response.getReply()).contains("solution");
        assertThat(response.getReply()).contains("8080");
        assertThat(response.getResolvedByAi()).isFalse();
        assertThat(response.getSessionId()).isEqualTo(99L);
    }

    // ── Test 2 : Résolution NON déclenchée sur un message neutre ──────────────
    @Test
    @DisplayName("chat : message neutre → resolved = false, aucun ticket créé")
    void chat_messageNeutre_pasDeDeclenchementResolution() {
        String ollamaJson = """
            {"message": {"role": "assistant", "content": "Avez-vous essayé de redémarrer le serveur ?"}}
            """;

        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("prompt");
        when(llmClient.sendPrompt(anyString())).thenReturn(ollamaJson);
        mockSessionSave();

        AiRequest request = new AiRequest();
        request.setMessage("Mon serveur ne démarre pas");

        AiResponse response = aiService.chat(testUser, request);

        assertThat(response.getResolvedByAi()).isFalse();
        verify(ticketRepository, never()).save(any());
    }

    // ── Test 3 : Résolution DÉCLENCHÉE quand l'utilisateur confirme ───────────
    @Test
    @DisplayName("chat : l'utilisateur dit 'c'est résolu' → resolved = true, ticket créé")
    void chat_utilisateurConfirmeResolution_ticketCreeResolu() {
        String ollamaJson = """
            {"message": {"role": "assistant", "content": "Parfait, je suis ravi que votre problème soit réglé !"}}
            """;

        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("prompt");
        when(llmClient.sendPrompt(anyString())).thenReturn(ollamaJson);
        when(ticketRepository.save(any())).thenAnswer(inv -> {
            var t = inv.getArgument(0, com.minidesk.entity.Ticket.class);
            t.setId(77L);
            return t;
        });
        mockSessionSave();

        AiRequest request = new AiRequest();
        request.setMessage("c'est résolu merci !");

        AiResponse response = aiService.chat(testUser, request);

        assertThat(response.getResolvedByAi()).isTrue();
        verify(ticketRepository, times(1)).save(any());
    }

    // ── Test 4 : Le LLM retourne une erreur → réponse de secours ─────────────
    @Test
    @DisplayName("chat : erreur LLM → retourne message de secours, aucune exception")
    void chat_erreurLlm_retourneMessageDeSecours() {
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("prompt");
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("Impossible de joindre Ollama"));

        AiRequest request = new AiRequest();
        request.setMessage("problème réseau");

        AiResponse response = aiService.chat(testUser, request);

        assertThat(response).isNotNull();
        assertThat(response.getReply()).contains("erreur");
        assertThat(response.getResolvedByAi()).isFalse();
        assertThat(response.getSuggestedCreateTicket()).isFalse();
    }

    // ── Test 5 : Session multi-messages analyse toute la conversation ──────────
    @Test
    @DisplayName("generateTicketInformation : session multi-messages → envoie toute la conversation et retourne la synthèse")
    void generateTicketInformation_sessionMultiMessages_analyseTouteLaConversation() {
        List<Map<String, String>> messagesList = List.of(
                Map.of("role", "user", "content", "Je n'arrive pas à me connecter au Wi-Fi."),
                Map.of("role", "assistant", "content", "Redémarrez votre ordinateur."),
                Map.of("role", "user", "content", "Je l'ai fait mais ça ne fonctionne toujours pas."),
                Map.of("role", "assistant", "content", "Redémarrez votre box."),
                Map.of("role", "user", "content", "Je viens de le faire, toujours aucun changement.")
        );

        AiSession session = AiSession.builder()
                .id(10L)
                .user(testUser)
                .messages(objectMapper.valueToTree(messagesList))
                .build();

        String aiResponseJson = """
            {
              "message": {
                "role": "assistant",
                "content": "{\\"title\\": \\"Problème de connexion Wi-Fi\\", \\"description\\": \\"Problème : L'utilisateur ne parvient pas à se connecter au réseau Wi-Fi.\\\\nActions déjà effectuées :\\\\n- Redémarrage de l'ordinateur : problème toujours présent.\\\\n- Redémarrage de la box : problème toujours présent.\\\\nRésultat : Le problème persiste malgré les différentes manipulations.\\", \\"category\\": \\"NETWORK\\", \\"priority\\": \\"HAUTE\\"}"
              }
            }
            """;

        when(promptBuilder.buildTicketGenerationPrompt(session)).thenReturn("prompt complet");
        when(llmClient.sendPrompt("prompt complet")).thenReturn(aiResponseJson);

        TicketGenerationResult result = aiService.generateTicketInformation(session);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Problème de connexion Wi-Fi");
        assertThat(result.description()).contains("Redémarrage de l'ordinateur");
        assertThat(result.description()).contains("Redémarrage de la box");
        assertThat(result.category()).isEqualTo(TicketCategory.NETWORK);
        assertThat(result.priority()).isEqualTo(Priority.HAUTE);

        verify(promptBuilder).buildTicketGenerationPrompt(session);
    }

    // ── Test 6 : Le dernier message n'est PAS utilisé seul comme description ─
    @Test
    @DisplayName("createTicketFromSession : la description générée n'est pas simplement le dernier message")
    void createTicketFromSession_descriptionAutomatique_nePrendPasUniquementLeDernierMessage() {
        List<Map<String, String>> messagesList = List.of(
                Map.of("role", "user", "content", "Message initial : Mon imprimante ne marche plus"),
                Map.of("role", "assistant", "content", "Vérifiez le câble USB"),
                Map.of("role", "user", "content", "Dernier message : toujours rien")
        );

        AiSession session = AiSession.builder()
                .id(15L)
                .user(testUser)
                .messages(objectMapper.valueToTree(messagesList))
                .build();

        String aiResponseJson = """
            {
              "message": {
                "role": "assistant",
                "content": "{\\"title\\": \\"Panne Imprimante\\", \\"description\\": \\"Synthèse globale : Problème imprimante ne répond pas. Vérification câble effectuée sans succès.\\", \\"category\\": \\"HARDWARE\\", \\"priority\\": \\"NORMALE\\"}"
              }
            }
            """;

        when(promptBuilder.buildTicketGenerationPrompt(session)).thenReturn("prompt");
        when(llmClient.sendPrompt(anyString())).thenReturn(aiResponseJson);
        when(ticketRepository.save(any())).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        CreateTicketRequest req = new CreateTicketRequest(); // description null

        TicketResponse response = aiService.createTicketFromSession(session, req, testUser);

        assertThat(response.getDescription()).isNotEqualTo("Dernier message : toujours rien");
        assertThat(response.getDescription()).contains("Synthèse globale");
        assertThat(response.getCategory()).isEqualTo(TicketCategory.HARDWARE);
    }

    // ── Test 7 : Conversion correcte de TicketCategory ───────────────────────
    @Test
    @DisplayName("generateTicketInformation : category est correctement convertie vers TicketCategory")
    void generateTicketInformation_categoryValide_convertitEnEnumTicketCategory() {
        AiSession session = AiSession.builder()
                .id(1L)
                .messages(objectMapper.valueToTree(List.of(Map.of("role", "user", "content", "Erreur mot de passe"))))
                .build();

        String aiResponseJson = """
            {
              "message": {
                "content": "{\\"title\\": \\"Accès bloqué\\", \\"description\\": \\"Mot de passe incorrect\\", \\"category\\": \\"ACCESS\\", \\"priority\\": \\"NORMALE\\"}"
              }
            }
            """;

        when(promptBuilder.buildTicketGenerationPrompt(any())).thenReturn("prompt");
        when(llmClient.sendPrompt(anyString())).thenReturn(aiResponseJson);

        TicketGenerationResult result = aiService.generateTicketInformation(session);

        assertThat(result.category()).isEqualTo(TicketCategory.ACCESS);
    }

    // ── Test 8 : Conversion correcte de Priority ─────────────────────────────
    @Test
    @DisplayName("generateTicketInformation : priority est correctement convertie vers Priority")
    void generateTicketInformation_priorityValide_convertitEnEnumPriority() {
        AiSession session = AiSession.builder()
                .id(1L)
                .messages(objectMapper.valueToTree(List.of(Map.of("role", "user", "content", "Serveur en panne"))))
                .build();

        String aiResponseJson = """
            {
              "message": {
                "content": "{\\"title\\": \\"Panne Serveur\\", \\"description\\": \\"Serveur principal indisponible\\", \\"category\\": \\"SOFTWARE\\", \\"priority\\": \\"CRITIQUE\\"}"
              }
            }
            """;

        when(promptBuilder.buildTicketGenerationPrompt(any())).thenReturn("prompt");
        when(llmClient.sendPrompt(anyString())).thenReturn(aiResponseJson);

        TicketGenerationResult result = aiService.generateTicketInformation(session);

        assertThat(result.priority()).isEqualTo(Priority.CRITIQUE);
    }

    // ── Test 9 : Gestion de valeur invalide (Fallback) ───────────────────────
    @Test
    @DisplayName("generateTicketInformation : category et priority invalides → utilise les valeurs par défaut (OTHER, NORMALE)")
    void generateTicketInformation_valeursInvalides_utiliseValeursParDefaut() {
        AiSession session = AiSession.builder()
                .id(1L)
                .messages(objectMapper.valueToTree(List.of(Map.of("role", "user", "content", "Bug bizarre"))))
                .build();

        // category et priority contiennent des valeurs inexistantes dans les enums
        String aiResponseJson = """
            {
              "message": {
                "content": "{\\"title\\": \\"Bug\\", \\"description\\": \\"Description du bug\\", \\"category\\": \\"CATEGORIE_INVENTEE\\", \\"priority\\": \\"ULTRA_HAUTE\\"}"
              }
            }
            """;

        when(promptBuilder.buildTicketGenerationPrompt(any())).thenReturn("prompt");
        when(llmClient.sendPrompt(anyString())).thenReturn(aiResponseJson);

        TicketGenerationResult result = aiService.generateTicketInformation(session);

        // Ne doit pas lever d'exception, mais utiliser les fallbacks
        assertThat(result.category()).isEqualTo(TicketCategory.OTHER);
        assertThat(result.priority()).isEqualTo(Priority.NORMALE);
    }

    // ── Test 10 : Session vide ou sans messages ──────────────────────────────
    @Test
    @DisplayName("generateTicketInformation : session vide ou sans messages → gérée proprement sans exception")
    void generateTicketInformation_sessionVide_retourneValeursParDefaut() {
        AiSession emptySession = AiSession.builder()
                .id(20L)
                .messages(objectMapper.createArrayNode())
                .build();

        TicketGenerationResult result = aiService.generateTicketInformation(emptySession);

        assertThat(result).isNotNull();
        assertThat(result.title()).isNotEmpty();
        assertThat(result.description()).contains("Aucun historique");
        assertThat(result.category()).isEqualTo(TicketCategory.OTHER);
        assertThat(result.priority()).isEqualTo(Priority.NORMALE);

        // Le LLM ne doit même pas être appelé si la session est vide
        verify(llmClient, never()).sendPrompt(anyString());
    }

    // ── Test 11 : Le ticket final contient description, category et priority générées ─
    @Test
    @DisplayName("createTicketFromSession : le ticket final contient description, category et priority générées")
    void createTicketFromSession_ticketFinalContientToutesLesInfosGenerees() {
        AiSession session = AiSession.builder()
                .id(30L)
                .user(testUser)
                .messages(objectMapper.valueToTree(List.of(
                        Map.of("role", "user", "content", "Problème d'imprimante réseau"),
                        Map.of("role", "assistant", "content", "Avez-vous testé le ping ?")
                )))
                .build();

        String aiResponseJson = """
            {
              "message": {
                "content": "{\\"title\\": \\"Imprimante Réseau Inaccessible\\", \\"description\\": \\"Problème d'impression réseau. Ping non testé par l'utilisateur.\\", \\"category\\": \\"HARDWARE\\", \\"priority\\": \\"HAUTE\\"}"
              }
            }
            """;

        when(promptBuilder.buildTicketGenerationPrompt(session)).thenReturn("prompt");
        when(llmClient.sendPrompt(anyString())).thenReturn(aiResponseJson);
        when(ticketRepository.save(any())).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(200L);
            return t;
        });

        CreateTicketRequest req = new CreateTicketRequest();

        TicketResponse response = aiService.createTicketFromSession(session, req, testUser);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getTitle()).isEqualTo("Imprimante Réseau Inaccessible");
        assertThat(response.getDescription()).isEqualTo("Problème d'impression réseau. Ping non testé par l'utilisateur.");
        assertThat(response.getCategory()).isEqualTo(TicketCategory.HARDWARE);
        assertThat(response.getPriority()).isEqualTo(Priority.HAUTE);
        assertThat(response.getStatus()).isEqualTo(com.minidesk.enums.TicketStatus.OPEN);
        assertThat(response.getCreatedById()).isEqualTo(testUser.getId());
    }
}

