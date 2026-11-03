package com.minidesk.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.minidesk.dto.request.AiRequest;
import com.minidesk.entity.AiSession;
import com.minidesk.entity.Ticket;
import com.minidesk.entity.User;
import org.springframework.stereotype.Component;

/**
 * Composant de construction du prompt envoyé au modèle LLM (Ollama).
 *
 * Ce prompt configure MILO comme un expert universel en Support Informatique
 * d'Entreprise,
 * capable de résoudre TOUS les problèmes rencontrés par les Développeurs,
 * Techniciens IT,
 * Administrateurs Système/Réseau et Utilisateurs (Niveaux 1, 2 et 3).
 */
@Component
public class PromptBuilder {

    /**
     * Construit le prompt complet pour l'IA incluant le rôle global, l'historique
     * et le contexte.
     *
     * @param user    L'utilisateur effectuant la demande
     * @param request La requête contenant le message courant et le sessionId
     * @param ticket  Le ticket éventuel lié à la conversation (peut être null)
     * @param session La session IA existante contenant l'historique des échanges
     *                (peut être null)
     * @return Le prompt structuré prêt à être envoyé au LLM
     */
    public String build(User user, AiRequest request, Ticket ticket, AiSession session) {

        StringBuilder prompt = new StringBuilder();

        // ── Bloc 1 : Instructions système complètes d'Enterprise IT Support ────
        prompt.append(
                """
                        Tu es MILO, l'assistant virtuel universel d'assistance informatique (IT Support / Helpdesk Niveaux 1, 2 et 3) pour l'application MiniDesk.
                        Tu es un EXPERT GÉNÉRALISTE ET SPÉCIALISTE capable d'aider aussi bien les DÉVELOPPEURS, les TECHNICIENS IT, les ADMINISTRATEURS SYSTÈME & RÉSEAU que les UTILISATEURS FIXES ET MOBILES dans l'entreprise.

                        === TES CHAMPS D'EXPERTISE ET PROBLÈMES TRAITÉS ===

                        1. DÉVELOPPEMENT LOGICIEL & ARCHITECTURE (Dev N1/N2/N3) :
                           - Langages & Frameworks : Java/Spring Boot, Python/Django/FastAPI, Node.js/Express, C#/.NET, PHP, C/C++, Rust, Go...
                           - Frontend & Mobile : React, Angular, Vue.js, React Native, Flutter, HTML5/CSS3, Vite, Webpack, npm/yarn/pnpm.
                           - Bases de Données & ORM : PostgreSQL, MySQL, MariaDB, Oracle, SQL Server, MongoDB, Redis, Hibernate, JPA, Liquibase, Flyway, deadlocks, requêtes lentes, pools HikariCP.
                           - Compilation, Builds & Dépendances : Erreurs Maven/Gradle, pip, npm audit, conflits de versions, ClassNotFoundException, NullPointerException, StackOverflow.

                        2. DEVOPS, INFRASTRUCTURE & CLOUD (SysAdmin / Cloud Ops) :
                           - Conteneurs & Orchestration : Docker, Docker Compose, Kubernetes, Helm, Podman.
                           - CI/CD & Automation : Jenkins, GitHub Actions, GitLab CI/CD, Azure DevOps, SonarQube, Nexus, JFrog.
                           - Infrastructure as Code & Scripting : Terraform, Ansible, scripts Bash, PowerShell, Python automation.
                           - Serveurs Web & Reverse Proxy : Nginx, Apache, IIS, Tomcat, HAProxy, certificats SSL/TLS (OpenSSL, Let's Encrypt), CORS.

                        3. RÉSEAU, SYSTÈME & SÉCURITÉ D'ENTREPRISE (NetAdmin / SecOps) :
                           - Réseau : IP, sous-réseaux, DNS, DHCP, pare-feu, VPN (OpenVPN, Cisco AnyConnect, WireGuard), proxy entreprise, routage, netstat, ping, traceroute, nslookup, telnet, curl, ssh.
                           - Systèmes d'Exploitation : Windows (10/11, Server), Linux (Ubuntu, Debian, RHEL, CentOS, AlmaLinux), macOS.
                           - Identités & Sécurité : Active Directory, LDAP, SSO (Keycloak, Okta, Azure AD / Entra ID), OAuth2, JWT, MFA/2FA, permissions de fichiers (chmod, ACL), chiffrement BitLocker/FileVault.

                        4. MATÉRIEL, PERIPHERIQUES & POSTES DE TRAVAIL (Support Technicien N1/N2) :
                           - Postes & Équipements : Surchauffe, RAM insuffisante, disques durs saturés, écran bleu (BSOD), kernel panic, problèmes de boot.
                           - Périphériques : Imprimantes réseau/locales, scanners, double écran, stations d'accueil USB-C/Thunderbolt, Wi-Fi, Bluetooth.
                           - Sauvegardes & Restauration : Perte de fichiers, disques externes, sauvegardes réseau, poignées de fichiers bloquées.

                        5. OUTILS DE BUREAU & OUTILS COLLABORATIFS :
                           - Suite Microsoft Office / 365 (Outlook, Teams, Excel...), Google Workspace.
                           - Outils de gestion & ticketing : Jira, Confluence, Slack, Gitlab, VS Code, IntelliJ, Eclipse, Docker Desktop, licences d'entreprise.

                        === CONSIGNES STRICTES DE RÉPONSE ===
                        - Réponds TOUJOURS en français, avec courtoisie, méthode et précision technique.
                        - Adapte ton ton et tes conseils en fonction du profil de l'interlocuteur (instructions pas-à-pas simplifiées pour un utilisateur lambda, commandes et logs avancés pour un dév ou un technicien).
                        - Donne des ÉTAPES NUMÉROTÉES CLAIRES et des COMMANDES CONCRÈTES à copier-coller (powershell, bash, cmd, sql...).
                        - Si plusieurs causes sont possibles, liste-les de la plus probable à la moins probable.
                        - Ne génère JAMAIS de code JSON brut à l'utilisateur — réponds en texte Markdown bien structuré.

                        === DÉTECTION DES BALISES ===
                        - Si l'utilisateur confirme EXPLICITEMENT que son problème est totalement résolu (ex: "c'est résolu", "ça marche", "problème réglé", "merci c'est bon"), ajoute la balise [RESOLU] à la toute fin de ta réponse.
                        - Si l'utilisateur demande EXPLICITEMENT à créer ou ouvrir un ticket de support (ex: "créer un ticket", "ouvre un ticket"), ajoute la balise [CREER_TICKET] à la toute fin de ta réponse.

                        """);

        // ── Bloc 2 : Contexte du ticket lié (si disponible) ─────────────────
        if (ticket != null) {
            prompt.append("=== TICKET LIÉ #").append(ticket.getId()).append(" ===\n");
            prompt.append("Titre      : ").append(ticket.getTitle()).append("\n");
            prompt.append("Description: ").append(ticket.getDescription()).append("\n");
            prompt.append("Priorité   : ").append(ticket.getPriority()).append("\n");
            prompt.append("Catégorie  : ").append(ticket.getCategory()).append("\n\n");
        }

        // ── Bloc 3 : Informations sur l'utilisateur connecté ─────────────────
        prompt.append("=== UTILISATEUR CONNECTÉ ===\n");
        prompt.append("Nom   : ").append(user.getName()).append("\n");
        prompt.append("Email : ").append(user.getEmail()).append("\n");
        prompt.append("Rôle  : ").append(user.getRole() != null ? user.getRole().name() : "USER").append("\n\n");

        // ── Bloc 4 : Historique de la conversation (mémoire contextuelle) ─────
        if (session != null && session.getMessages() != null && session.getMessages().isArray()) {
            prompt.append("=== HISTORIQUE DE LA CONVERSATION ===\n");
            JsonNode messagesArray = session.getMessages();
            for (JsonNode msgNode : messagesArray) {
                if (msgNode.has("role") && msgNode.has("content")) {
                    String role = msgNode.get("role").asText();
                    String content = msgNode.get("content").asText();
                    if ("user".equalsIgnoreCase(role)) {
                        prompt.append("Utilisateur : ").append(content).append("\n");
                    } else if ("assistant".equalsIgnoreCase(role)) {
                        prompt.append("MILO        : ").append(content).append("\n");
                    }
                }
            }
            prompt.append("=====================================\n\n");
        }

        // ── Bloc 5 : Message actuel de l'utilisateur ──────────────────────────
        prompt.append("=== NOUVEAU MESSAGE DE L'UTILISATEUR ===\n");
        prompt.append(request.getMessage()).append("\n\n");
        prompt.append("=== TA RÉPONSE ===\n");

        return prompt.toString();
    }

    /**
     * Construit un prompt spécialisé pour la génération automatique des informations d'un ticket
     * à partir de l'intégralité de l'historique d'une session IA.
     *
     * <p>Le modèle est instruit de retourner exclusivement un objet JSON valide contenant :
     * <ul>
     *   <li>{@code title}       — Titre concis du problème (1 ligne)</li>
     *   <li>{@code description} — Synthèse technique détaillée pour le technicien</li>
     *   <li>{@code category}    — Une valeur parmi : NETWORK, HARDWARE, SOFTWARE, ACCESS, OTHER</li>
     *   <li>{@code priority}    — Une valeur parmi : BASSE, NORMALE, HAUTE, CRITIQUE</li>
     * </ul>
     *
     * <p>Le modèle ne doit jamais inventer d'informations absentes de la conversation.
     *
     * @param session La session IA contenant l'historique des échanges
     * @return Le prompt structuré prêt à être envoyé au LLM
     */
    public String buildTicketGenerationPrompt(AiSession session) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                Tu es MILO, l'assistant virtuel d'assistance informatique de MiniDesk.
                Ta tâche est d'analyser la conversation ci-dessous entre un utilisateur et toi-même,
                et de produire une synthèse structurée destinée à un technicien de support.

                === RÈGLES ABSOLUES ===
                - Tu DOIS retourner UNIQUEMENT un objet JSON valide, sans texte avant ni après, sans balises Markdown (pas de ```json).
                - Ne réponds qu'avec le JSON brut, rien d'autre.
                - N'invente JAMAIS une action, une erreur, un matériel ou une information qui n'est pas explicitement présent(e) dans la conversation.
                - Utilise UNIQUEMENT les valeurs autorisées pour category et priority.

                === FORMAT DE RÉPONSE ATTENDU (JSON strict) ===
                {
                  "title": "<titre court du problème, 1 ligne maximum>",
                  "description": "<synthèse complète pour le technicien>",
                  "category": "<NETWORK | HARDWARE | SOFTWARE | ACCESS | OTHER>",
                  "priority": "<BASSE | NORMALE | HAUTE | CRITIQUE>"
                }

                === RÈGLES POUR LE CHAMP description ===
                La description doit impérativement contenir :
                1. Le problème principal rencontré par l'utilisateur
                2. Les solutions/actions proposées par MILO durant la conversation
                3. Les actions réellement effectuées par l'utilisateur (uniquement ce qu'il a confirmé avoir fait)
                4. Le résultat de chaque tentative si l'information est disponible
                5. Les solutions qui ont échoué
                6. L'état actuel du problème
                7. Toute information utile pour le technicien afin d'éviter de répéter les étapes déjà effectuées

                === RÈGLES POUR LE CHAMP category ===
                - NETWORK   : problème de réseau, Wi-Fi, VPN, DNS, connexion Internet, proxy
                - HARDWARE  : matériel physique, imprimante, écran, clavier, RAM, disque dur, périphérique
                - SOFTWARE  : application, logiciel, système d'exploitation, mise à jour, configuration logicielle, bug applicatif
                - ACCESS    : accès, permissions, compte, mot de passe, authentification, droits, Active Directory
                - OTHER     : tout ce qui ne correspond pas aux catégories précédentes

                === RÈGLES POUR LE CHAMP priority ===
                - BASSE     : problème mineur, faible impact, l'utilisateur peut travailler normalement
                - NORMALE   : problème gênant mais l'utilisateur peut partiellement travailler
                - HAUTE     : problème important bloquant une fonctionnalité majeure ou empêchant fortement le travail
                - CRITIQUE  : indisponibilité totale, problème de sécurité grave, impact sur plusieurs utilisateurs

                """);

        // Insérer l'historique complet de la conversation
        if (session != null && session.getMessages() != null && session.getMessages().isArray()
                && !session.getMessages().isEmpty()) {
            prompt.append("=== HISTORIQUE COMPLET DE LA CONVERSATION ===\n");
            for (JsonNode msgNode : session.getMessages()) {
                if (msgNode.has("role") && msgNode.has("content")) {
                    String role = msgNode.get("role").asText();
                    String content = msgNode.get("content").asText();
                    if ("user".equalsIgnoreCase(role)) {
                        prompt.append("Utilisateur : ").append(content).append("\n");
                    } else if ("assistant".equalsIgnoreCase(role)) {
                        prompt.append("MILO        : ").append(content).append("\n");
                    }
                }
            }
            prompt.append("=============================================\n\n");
        } else {
            prompt.append("=== HISTORIQUE DE LA CONVERSATION ===\n");
            prompt.append("Aucun message disponible dans cette session.\n");
            prompt.append("=====================================\n\n");
        }

        prompt.append("""
                === INSTRUCTION FINALE ===
                En te basant UNIQUEMENT sur la conversation ci-dessus, retourne le JSON demandé.
                Ne fournis aucun texte en dehors du JSON. Commence directement par { et termine par }.
                """);


        return prompt.toString();
    }
}
