package com.minidesk.config;

import com.minidesk.entity.KnowledgeArticle;
import com.minidesk.entity.User;
import com.minidesk.enums.TicketCategory;
import com.minidesk.repository.KnowledgeArticleRepository;
import com.minidesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initialise des articles de FAQ / Base de connaissances au démarrage si la table est vide.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseInitializer implements CommandLineRunner {

    private final KnowledgeArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (articleRepository.count() > 0) {
            return;
        }

        User author = userRepository.findAll().stream().findFirst().orElse(null);
        if (author == null) {
            return;
        }

        log.info("Initialisation de la base de connaissances (FAQ)...");

        KnowledgeArticle a1 = KnowledgeArticle.builder()
                .title("Comment réinitialiser son mot de passe de session ?")
                .category(TicketCategory.ACCESS)
                .keywords(List.of("mot de passe", "login", "accès", "reset", "compte"))
                .author(author)
                .content("""
                        Pour réinitialiser votre mot de passe d'accès :
                        1. Rendez-vous sur la page Mon Profil dans le menu latéral.
                        2. Dans la section Sécurité, saisissez votre mot de passe actuel.
                        3. Renseignez un nouveau mot de passe fort d'au moins 8 caractères contenant majuscules, minuscules et chiffres.
                        4. Cliquez sur 'Mettre à jour le mot de passe'.
                        En cas d'oubli complet, contactez un administrateur à admin@minidesk.com.
                        """)
                .build();

        KnowledgeArticle a2 = KnowledgeArticle.builder()
                .title("Résolution des problèmes de connexion VPN Cisco")
                .category(TicketCategory.NETWORK)
                .keywords(List.of("vpn", "cisco", "réseau", "télétravail", "connexion"))
                .author(author)
                .content("""
                        Si la connexion au VPN échoue (erreur 412 ou Handshake Failed) :
                        1. Vérifiez que votre box Internet fonctionne correctement.
                        2. Déconnectez et reconnectez votre adaptateur Wi-Fi ou câble Ethernet.
                        3. Redémarrez le client Cisco AnyConnect Secure Mobility.
                        4. Assurez-vous que l'adresse du serveur distant est bien : vpn.minidesk.local.
                        """)
                .build();

        KnowledgeArticle a3 = KnowledgeArticle.builder()
                .title("Configuration et ajout d'une imprimante réseau")
                .category(TicketCategory.HARDWARE)
                .keywords(List.of("imprimante", "impression", "réseau", "driver", "matériel"))
                .author(author)
                .content("""
                        Pour installer une imprimante réseau d'étage :
                        1. Ouvrez Paramètres Windows > Périphériques > Imprimantes et scanners.
                        2. Cliquez sur 'Ajouter une imprimante ou un scanner'.
                        3. Choisissez 'L'imprimante souhaitée n'est pas répertoriée'.
                        4. Sélectionnez 'Ajouter une imprimante à l'aide d'une adresse TCP/IP' et indiquez l'IP fournie par le support.
                        """)
                .build();

        articleRepository.saveAll(List.of(a1, a2, a3));
        log.info("3 articles de démonstration ajoutés à la base de connaissances !");
    }
}
