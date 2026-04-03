package com.pecodigos.dbarena.config.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Date;

@Component
public class JwtUtil {
    static final String LOCAL_DEVELOPMENT_FALLBACK_SECRET = "db-arena-local-dev-secret-change-me";

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${app.frontend-origin}")
    private String frontendOrigin;

    @PostConstruct
    void validateConfiguration() {
        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            throw new IllegalStateException("DB_ARENA_JWT_SECRET must be configured.");
        }

        if (LOCAL_DEVELOPMENT_FALLBACK_SECRET.equals(SECRET_KEY)) {
            if (!isLocalDevelopmentEnvironment()) {
                throw new IllegalStateException("DB_ARENA_JWT_SECRET must be configured outside local development.");
            }

            log.warn("Using built-in local development JWT secret. Set DB_ARENA_JWT_SECRET before deploying or sharing this environment.");
        }

        if (EXPIRATION_TIME <= 0) {
            throw new IllegalStateException("jwt.expiration must be greater than zero.");
        }
    }

    private boolean isLocalDevelopmentEnvironment() {
        return isLocalDatasourceUrl(datasourceUrl)
                && isLocalHost(redisHost)
                && isLocalOrigin(frontendOrigin);
    }

    private boolean isLocalDatasourceUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String normalizedUrl = url.trim().toLowerCase();
        return normalizedUrl.contains(":h2:mem:")
                || normalizedUrl.contains("://localhost")
                || normalizedUrl.contains("://127.0.0.1")
                || normalizedUrl.contains("://[::1]");
    }

    private boolean isLocalOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }

        try {
            return isLocalHost(URI.create(origin.trim()).getHost());
        } catch (IllegalArgumentException ignored) {
            String normalizedOrigin = origin.trim().toLowerCase();
            return normalizedOrigin.contains("localhost")
                    || normalizedOrigin.contains("127.0.0.1")
                    || normalizedOrigin.contains("[::1]");
        }
    }

    private boolean isLocalHost(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }

        String normalizedHost = host.trim().toLowerCase();
        return "localhost".equals(normalizedHost)
                || "127.0.0.1".equals(normalizedHost)
                || "::1".equals(normalizedHost)
                || "[::1]".equals(normalizedHost);
    }

    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public String validateToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY)).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        return decodedJWT.getSubject();
    }
}
