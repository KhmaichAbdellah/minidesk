package com.minidesk.config;

import com.minidesk.entity.Ticket;
import com.minidesk.entity.User;
import com.minidesk.enums.Priority;
import com.minidesk.enums.Role;
import com.minidesk.enums.TicketCategory;
import com.minidesk.enums.TicketStatus;
import com.minidesk.repository.TicketRepository;
import com.minidesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Composant d'initialisation des données au démarrage de l'application.
 *
 * Crée automatiquement :
 *   1. Le compte Administrateur par défaut (admin@minidesk.com / Admin@2026!)
 *   2. Des utilisateurs de test (Techniciens & Développeurs/Users)
 *   3. Des tickets de démo avec différentes catégories, priorités et statuts
 */
@Component
@ConditionalOnProperty(name = "app.admin.create", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.name:Admin MiniDesk}")
    private String adminName;

    @Value("${app.admin.email:admin@minidesk.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@2026!}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        log.info("DataInitializer : vérification de l'initialisation des données de démo...");

        try {
            //  Creer le compte Administrateur si absent ou synchroniser son mot de passe
            User admin = userRepository.findByEmail(adminEmail).orElse(null);
            if (admin == null) {
                log.info("Création du compte Administrateur principal...");
                admin = User.builder()
                        .name(adminName)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build();
                admin = userRepository.save(admin);
                log.info("Compte Admin créé avec succès : {}", adminEmail);
            } else if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
                log.info("Mise à jour du mot de passe de l'administrateur : {}", adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                userRepository.save(admin);
            }


            User techSophie = userRepository.findByEmail("tech.sophie@minidesk.com").orElse(null);
            if (techSophie == null) {
                techSophie = User.builder()
                        .name("Sophie Martin (Technicienne Network)")
                        .email("tech.sophie@minidesk.com")
                        .password(passwordEncoder.encode("Tech@2026!"))
                        .role(Role.TECHNICIAN)
                        .enabled(true)
                        .build();
                techSophie = userRepository.save(techSophie);
            }

            User techMarc = userRepository.findByEmail("tech.marc@minidesk.com").orElse(null);
            if (techMarc == null) {
                techMarc = User.builder()
                        .name("Marc Dubois (Technicien Hardware)")
                        .email("tech.marc@minidesk.com")
                        .password(passwordEncoder.encode("Tech@2026!"))
                        .role(Role.TECHNICIAN)
                        .enabled(true)
                        .build();
                techMarc = userRepository.save(techMarc);
            }


            User devThomas = userRepository.findByEmail("dev.thomas@minidesk.com").orElse(null);
            if (devThomas == null) {
                devThomas = User.builder()
                        .name("Thomas Leroy (Dev Fullstack)")
                        .email("dev.thomas@minidesk.com")
                        .password(passwordEncoder.encode("Dev@2026!"))
                        .role(Role.USER)
                        .enabled(true)
                        .build();
                devThomas = userRepository.save(devThomas);
            }

            User userJulie = userRepository.findByEmail("user.julie@minidesk.com").orElse(null);
            if (userJulie == null) {
                userJulie = User.builder()
                        .name("Julie Benali (Comptabilité)")
                        .email("user.julie@minidesk.com")
                        .password(passwordEncoder.encode("User@2026!"))
                        .role(Role.USER)
                        .enabled(true)
                        .build();
                userJulie = userRepository.save(userJulie);
            }

            // creeation des tickets
            if (ticketRepository.count() == 0) {
                log.info("Création de 6 tickets de démonstration avec différentes catégories et priorités...");

                Ticket t1 = Ticket.builder()
                        .title("Erreur de connexion à la base de données PostgreSQL")
                        .description("Impossible de se connecter sur le port 5432 sur le serveur de dev. Message: Connection refused. Plusieurs dévs sont bloqués.")
                        .category(TicketCategory.NETWORK)
                        .priority(Priority.CRITIQUE)
                        .status(TicketStatus.OPEN)
                        .createdBy(devThomas)
                        .assignedTo(techSophie)
                        .build();

                Ticket t2 = Ticket.builder()
                        .title("Écran bleu (BSOD) récurrent au démarrage du poste")
                        .description("Mon ordinateur affiche un écran bleu SYSTEM_SERVICE_EXCEPTION 2 minutes après le démarrage du système.")
                        .category(TicketCategory.HARDWARE)
                        .priority(Priority.HAUTE)
                        .status(TicketStatus.PROGRESS)
                        .createdBy(userJulie)
                        .assignedTo(techMarc)
                        .build();

                Ticket t3 = Ticket.builder()
                        .title("Demande d'accès au dépôt Git d'entreprise minidesk-backend")
                        .description("Besoin des permissions de push sur le repo GitLab minidesk-backend pour la nouvelle intégration du module IA.")
                        .category(TicketCategory.ACCESS)
                        .priority(Priority.NORMALE)
                        .status(TicketStatus.RESOLVED)
                        .createdBy(devThomas)
                        .assignedTo(techSophie)
                        .build();

                Ticket t4 = Ticket.builder()
                        .title("Problème de mémoire et lenteur du serveur Web")
                        .description("Le build npm dev prend plus de 5 minutes. Probable saturation de la mémoire vive RAM ou cache node_modules.")
                        .category(TicketCategory.SOFTWARE)
                        .priority(Priority.BASSE)
                        .status(TicketStatus.CLOSED)
                        .createdBy(userJulie)
                        .assignedTo(techMarc)
                        .build();

                Ticket t5 = Ticket.builder()
                        .title("Certificat SSL expiré sur l'environnement de Staging")
                        .description("Erreur NET::ERR_CERT_DATE_INVALID lors de l'accès à https://staging.minidesk.local en HTTPS.")
                        .category(TicketCategory.OTHER)
                        .priority(Priority.HAUTE)
                        .status(TicketStatus.OPEN)
                        .createdBy(devThomas)
                        .assignedTo(null)
                        .build();

                Ticket t6 = Ticket.builder()
                        .title("Panne du VPN d'entreprise depuis la maison")
                        .description("La connexion au VPN Cisco AnyConnect échoue avec le code d'erreur 412 (Handshake Failed).")
                        .category(TicketCategory.NETWORK)
                        .priority(Priority.CRITIQUE)
                        .status(TicketStatus.PROGRESS)
                        .createdBy(userJulie)
                        .assignedTo(techSophie)
                        .build();

                ticketRepository.saveAll(List.of(t1, t2, t3, t4, t5, t6));
                log.info("6 tickets de démonstration créés avec succès !");
            } else {
                log.info("Des tickets existent déjà en base. Étape d'initialisation des tickets ignorée.");
            }

        } catch (DataIntegrityViolationException ex) {
            log.warn("Certaines données de démo existent déjà en base de données.", ex);
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation des données :", e);
        }
    }
}
