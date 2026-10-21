package com.minidesk.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.minidesk.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Service de gestion des tokens JWT (JSON Web Token).
 *
 * Un JWT est composé de 3 parties séparées par des points :
 *   header.payload.signature
 *
 * Ce service gère :
 *   1. La génération du token (contenant email, rôle, ID utilisateur)
 *   2. L'extraction des claims (email, rôle, ID)
 *   3. La validation du token (signature + expiration)
 *
 * Propriétés configurables dans application.yaml :
 *   jwt.secret     → clé secrète HMAC-SHA256 pour signer/vérifier le token
 *   jwt.expiration → durée de validité en millisecondes (ex: 86400000 = 24h)
 */
@Service
public class JwtService {

    /**
     * Clé secrète pour signer les tokens JWT.
     * Doit faire au moins 32 caractères pour HMAC-SHA256.
     * Injectée depuis application.yaml → jwt.secret
     */
    @Value("${jwt.secret}")
    private String JWT_SECRET;

    /**
     * Durée de validité du token en millisecondes.
     * Injectée depuis application.yaml → jwt.expiration
     * Exemple : 86400000 = 24 heures
     */
    @Value("${jwt.expiration}")
    private long JWT_EXPIRATION_TIME;

    /**
     * Construit la clé de signature HMAC-SHA256 à partir du secret configuré.
     * Keys.hmacShaKeyFor() crée un objet Key compatible avec JJWT.
     *
     * @return Clé de signature {@link Key}
     */
    private Key getSignInKey() {
        // Convertit la chaîne secrète en tableau de bytes puis en clé HMAC
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    /**
     * Génère un token JWT signé pour l'utilisateur donné.
     *
     * Claims inclus dans le token :
     *   - sub (subject)  : email de l'utilisateur (identifiant principal)
     *   - role           : rôle de l'utilisateur (ADMIN, TECHNICIAN, USER)
     *   - userId         : ID numérique de l'utilisateur en base de données
     *   - iat (issuedAt) : date/heure de création du token
     *   - exp (expiration): date/heure d'expiration du token
     *
     * @param user L'utilisateur authentifié pour lequel générer le token
     * @return Token JWT signé sous forme de String (format compact)
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())                                      // "sub" : identifiant unique = email
                .claim("name", user.getName() != null ? user.getName() : "")   // Nom complet de l'utilisateur
                .claim("role", user.getRole().name())                          // Rôle en tant que String (ex: "ADMIN")
                .claim("userId", user.getId())                                 // ID de l'utilisateur (utile côté frontend)
                .issuedAt(new Date())                                          // Date de création du token
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME)) // Date d'expiration
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)            // Signature HMAC-SHA256
                .compact();                                                    // Sérialise en String "header.payload.signature"
    }

    /**
     * Extrait tous les claims (informations) du token JWT.
     *
     * Lève une exception si le token est invalide, expiré ou mal formé.
     * Cette méthode est la base de toutes les autres méthodes d'extraction.
     *
     * @param token Le token JWT à parser
     * @return {@link Claims} contenant tous les champs du payload
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSignInKey())  // Vérifie la signature avec la clé secrète
                .build()
                .parseSignedClaims(token)  // Parse et valide le token
                .getPayload();             // Retourne le payload (les claims)
    }

    /**
     * Extrait l'email (subject "sub") du token JWT.
     * L'email est utilisé pour retrouver l'utilisateur en base dans JwtAuthFilter.
     *
     * @param token Le token JWT
     * @return Email de l'utilisateur
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();  // "sub" = email
    }

    /**
     * Extrait le rôle de l'utilisateur du token JWT.
     * Utilisé par JwtAuthFilter pour construire l'autorité Spring Security.
     *
     * @param token Le token JWT
     * @return Rôle en String (ex: "ADMIN", "TECHNICIAN", "USER")
     */
    public String extractRole(String token) {
        // Récupère le claim custom "role" défini lors de la génération
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extrait l'ID numérique de l'utilisateur du token JWT.
     * Utile pour le frontend qui peut ainsi identifier l'utilisateur sans appel réseau.
     *
     * @param token Le token JWT
     * @return ID de l'utilisateur (Long), ou null si absent
     */
    public Long extractUserId(String token) {
        // "userId" est un claim custom ajouté lors de la génération
        return extractAllClaims(token).get("userId", Long.class);
    }

    /**
     * Vérifie si le token JWT est expiré.
     *
     * @param token Le token JWT à vérifier
     * @return true si le token est expiré, false sinon
     */
    public boolean isTokenExpired(String token) {
        // Extrait la date d'expiration ("exp") et la compare à maintenant
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());  // before(now) = expiré
    }

    /**
     * Valide le token JWT en vérifiant :
     *   1. Que l'email dans le token correspond à celui de l'utilisateur fourni
     *   2. Que le token n'est pas expiré
     *
     * Utilisé dans JwtAuthFilter pour s'assurer que le token appartient bien
     * à l'utilisateur trouvé en base de données.
     *
     * @param token Le token JWT à valider
     * @param user  L'utilisateur trouvé en base à partir de l'email extrait du token
     * @return true si le token est valide pour cet utilisateur, false sinon
     */
    public boolean isTokenValid(String token, User user) {
        String email = extractEmail(token);
        // Double vérification : email correspondant ET token non expiré
        return email.equals(user.getEmail()) && !isTokenExpired(token);
    }
}
