package edu.fcu.furniturerecyclingbackend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {

    private static final String SECRET_KEY = "your-secret-key";  // 用於簽名 JWT

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))  // 設定 1 天過期
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}

