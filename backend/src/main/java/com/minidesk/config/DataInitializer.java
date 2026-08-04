package com.minidesk.config;

import com.minidesk.entity.User;
import com.minidesk.enums.Role;
import com.minidesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Composant Spring execute automatiquement au demarrage de l'application
@Component
@ConditionalOnProperty(name = "app.admin.create", havingValue = "true", matchIfMissing = true)
// Lombok : constructeur injectant les dependances finales
@RequiredArgsConstructor
// Lombok : active les logs SLF4J (log.info, log.warn, etc.)
@Slf4j
public class DataInitializer implements CommandLineRunner {

    // Repository pour acceder a la table des utilisateurs
    private final UserRepository userRepository;

    // Encodeur pour hacher le mot de passe avant stockage en base de donnees
    private final PasswordEncoder passwordEncoder;

    // Nom de l'administrateur par defaut lu depuis application.yaml
    @Value("${app.admin.name:}")
    private String adminName;

    // Email de connexion de l'administrateur par defaut
    @Value("${app.admin.email:}")
    private String adminEmail;

    // Mot de passe en clair de l'administrateur (sera hache avant persistance)
    @Value("${app.admin.password:}")
    private String adminPassword;

    // Methode executee automatiquement au demarrage grace a CommandLineRunner.
    // Verifie si un admin existe deja, sinon en cree un.
    @Override
    public void run(String... args) {
        log.info("DataInitializer : verification de l existence d un administrateur...");

        if (adminName == null || adminName.isBlank()
                || adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.warn("Proprietes d'administration manquantes (app.admin.*). Aucun compte par defaut cree.");
            return;
        }

        // Verifie si au moins un utilisateur avec le role ADMIN existe en base
        boolean adminExists = userRepository.existsByRole(Role.ADMIN);

        if (adminExists) {
            // Admin deja present, pas besoin d'en creer un nouveau
            log.info("Un administrateur existe deja. Aucune action effectuee.");
            return;
        }

        // Aucun admin trouve : creation du compte administrateur par defaut
        log.info("Aucun administrateur trouve. Creation du compte par defaut...");

        // Construction de l'entite utilisateur avec le role ADMIN
        User admin = User.builder()
                .name(adminName)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        // Sauvegarde en base de donnees - gestion des conditions de concurrence / contrainte unique
        try {
            userRepository.save(admin);
            // Affichage des informations de connexion dans les logs au demarrage (NE PAS afficher le mot de passe)
            log.info("Compte administrateur cree avec succes !");
            log.info("  Email    : {}", adminEmail);
            log.warn("SECURITE : Modifiez le mot de passe par defaut apres votre premiere connexion !");
        } catch (DataIntegrityViolationException ex) {
            // Un autre processus a peut-etre cree l'admin en parallele (ou contrainte unique)
            log.warn("Echec de la creation de l'utilisateur administrateur : contrainte d'integrite. Verifiez s'il existe deja.", ex);
        }
    }
}
