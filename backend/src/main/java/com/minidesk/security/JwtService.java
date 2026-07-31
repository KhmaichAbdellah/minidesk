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
@Service
public class JwtService {
//    // key secret
//    private static final String JWT_SECRET = "mySecretKeyForMiniDeskJwtAuthentication2026";
//    // date  d expiration 24h
//    private static final long JWT_EXPIRATION_TIME = 86_4000_000;
    @Value("${jwt.secret}")
    private  String JWT_SECRET ;
    @Value("${jwt.expiration}")
    private long JWT_EXPIRATION_TIME;



    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }
    // GENERER UN TOKEN
    public String generateToken(User user) {

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    //return un claim pour recupere le mail et le role
    public Claims extractAllClaims(String token) {

        return  Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();


    }
    //email
    public String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }
    // role
    public String extractRole(String token){
        return extractAllClaims(token).get("role",String.class);
    }
    // VERIFICATION date d expiration
    public boolean isTokenExpired(String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }
    // validation de token
    public boolean isTokenValid(String token, User user) {

        String email = extractEmail(token);

        return email.equals(user.getEmail()) && !isTokenExpired(token);
    }



}
