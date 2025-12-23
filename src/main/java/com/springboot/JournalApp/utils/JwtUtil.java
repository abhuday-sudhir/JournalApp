package com.springboot.JournalApp.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private String SECRET_KEY="a2jh94jdkvmosm9dnc3s9jc4#4nivns@";

    public String generateToken(String username) {
        Map<String , Object> claims=new HashMap<>();
        return createToken(claims,username);
    }

    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .header().empty().add("typ","JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+ 1000*60*120))
                .signWith(getSigningKey())
                .compact();

    }
    //claim is payload
    public String extractUsername(String token)
    {
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token)
    {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Date extractExpiration(String token)
    {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token)
    {
        System.out.println("The expiration date is: "+extractExpiration(token));
        System.out.println("The current date is: "+new Date());
        boolean val=extractExpiration(token).before(new Date());
        System.out.println("Token expired: "+val);
        return val;
    }

    public boolean validateToken(String token , String username)
    {
        final String extractedUsername=extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private SecretKey getSigningKey()
    {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}
