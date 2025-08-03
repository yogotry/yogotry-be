package com.yogotry.global.jwt;

import com.yogotry.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenExpirationMs;

    private byte[] keyBytes;

    @PostConstruct
    public void init() {
        try {
            // Base64로 인코딩된 키면 디코딩 시도
            keyBytes = Base64.getDecoder().decode(secretKey);
        } catch (IllegalArgumentException e){
            // 디코딩 실패 시 평문 문자열로 간주하고 UTF-8 바이트로 변환
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    // 유효한 토큰인지 검증
    public boolean isValid(String token) {
        try{
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e){
            logger.warn("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e){
            logger.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e){
            logger.warn("Malformed JWT token: {}", e.getMessage());
        } catch (SignatureException e){
            logger.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e){
            logger.warn("Illegal argument token: {}", e.getMessage());
        }
        return false;
    }

    // 토큰에서 userId 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }

    // 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 토큰 만료일 추출
    public Date getExpiration(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }
}
