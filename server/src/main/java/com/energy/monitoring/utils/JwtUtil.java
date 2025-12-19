package com.energy.monitoring.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.energy.monitoring.components.HttpConstructions.UserRoles;
import com.energy.monitoring.config.Config;
import com.energy.monitoring.config.ConfigKeys;

/* Инструменты для работы с JWT-токенами */
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);                                             // Объект Logger для текущего класса

    private static final String    SECRET             = Config.getString(ConfigKeys.JsonWebToken.SECRET);                           // Ключ JWT-токена
    private static final long      JWT_TOKEN_VALIDITY = Config.getInt(ConfigKeys.JsonWebToken.EXPIRATION_HOURS) * (60 * 60 * 1000); // Время жизни JWT-токена в миллисекундах
    private static final String    ISSUER             = Config.getString(ConfigKeys.JsonWebToken.ISSUER);                           // Издатель JWT-токена
    private static final Algorithm ALGORITHM          = Algorithm.HMAC256(SECRET);                                                  // Алгоритм генерации токена
    
    // Возвращает строку с Web-токеном для пользователя с username и userId c ролью operator
    public static String generateToken(String username, Integer userId) {
        return generateToken(username, userId, UserRoles.OPERATOR);
    }

    // Возвращает строку с Web-токеном для пользователя с username и userId c ролью кщду
    public static String generateToken(String username, Integer userId, String role) {
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("typ", "JWT");
        
        try {
            String token = JWT.create()
                              .withHeader(headerClaims)
                              .withIssuer(ISSUER)
                              .withSubject(username)
                              .withClaim("id", userId)
                              .withClaim("role", role)
                              .withIssuedAt(new Date())
                              .withExpiresAt(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                              .withJWTId(java.util.UUID.randomUUID().toString())
                              .sign(ALGORITHM);
            return token;
        } catch (JWTCreationException e) {
            logger.error("Token creation failed: {}", e.getMessage());
            return null;
        }
    }
    
    // Возвращает true, если токен token коректен и актуален, иначе - false
    public static boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(ALGORITHM)
                                      .withIssuer(ISSUER)
                                      .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Возвращает расшифрованный токен token
    public static DecodedJWT decodeToken(String token) {
        try {
            return JWT.decode(token);
        } catch (JWTDecodeException e) {
            logger.error("Token decoding failed: {}", e.getMessage());
            return null;
        }
    }
    
    // Возвращает имя пользователя из токена token
    public static String getUsernameFromToken(String token) {
        DecodedJWT jwt  = JWT.decode(token);
        String username = jwt.getSubject();
        if (username == null) {
            logger.warn("Geted null-username");
        }

        return username;
    }
    
    // Возвращает id пользователя из токена token
    public static Integer getUserIdFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Integer userId = jwt.getClaim("id").asInt();
        if (userId == null) {
            logger.warn("Geted null-userID");
        }

        return userId;
    }

    // Возвращает роль пользователя из токена token
    public static String getRoleFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        String role    = jwt.getClaim("role").asString();
        if (role == null) {
            logger.warn("Geted null-role, set operator-role");
            role = UserRoles.OPERATOR;
        }

        return role;
    }
    
    // Возвращает true, если срок жизни токена token на истёк, иначе - false
    public static boolean isTokenExpired(String token) {
        DecodedJWT jwt  = JWT.decode(token);
        boolean expired = jwt.getExpiresAt().before(new Date());

        return expired;
    }
    
    // Возвращает время истечения токена token
    public static Date getExpirationDate(String token) {
        DecodedJWT jwt      = JWT.decode(token);
        Date expirationDate = jwt.getExpiresAt();
        if (expirationDate == null) {
            logger.warn("Geted null-expirationDate");
        }

        return expirationDate;
    }
    
    // Возвращается токен с обновлённым сроком жизни на основе oldToken
    public static String refreshToken(String oldToken) {
        return generateToken(getUsernameFromToken(oldToken), getUserIdFromToken(oldToken), getRoleFromToken(oldToken));
    }
}