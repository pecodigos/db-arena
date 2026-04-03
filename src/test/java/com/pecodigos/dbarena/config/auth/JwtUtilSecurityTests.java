package com.pecodigos.dbarena.config.auth;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilSecurityTests {

    @Test
    void validateConfigurationShouldRejectBlankSecret() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", "");
        ReflectionTestUtils.setField(jwtUtil, "EXPIRATION_TIME", 60_000L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(jwtUtil, "validateConfiguration")
        );

        assertEquals("DB_ARENA_JWT_SECRET must be configured.", exception.getMessage());
    }

    @Test
    void validateConfigurationShouldAllowFallbackSecretForLocalDevelopmentDefaults() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", JwtUtil.LOCAL_DEVELOPMENT_FALLBACK_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "EXPIRATION_TIME", 60_000L);
        ReflectionTestUtils.setField(jwtUtil, "datasourceUrl", "jdbc:postgresql://localhost:5432/db-arena");
        ReflectionTestUtils.setField(jwtUtil, "redisHost", "localhost");
        ReflectionTestUtils.setField(jwtUtil, "frontendOrigin", "http://localhost:4200");

        ReflectionTestUtils.invokeMethod(jwtUtil, "validateConfiguration");
    }

    @Test
    void validateConfigurationShouldRejectFallbackSecretOutsideLocalDevelopment() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", JwtUtil.LOCAL_DEVELOPMENT_FALLBACK_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "EXPIRATION_TIME", 60_000L);
        ReflectionTestUtils.setField(jwtUtil, "datasourceUrl", "jdbc:postgresql://db.prod.internal:5432/db-arena");
        ReflectionTestUtils.setField(jwtUtil, "redisHost", "redis.prod.internal");
        ReflectionTestUtils.setField(jwtUtil, "frontendOrigin", "https://arena.example.com");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(jwtUtil, "validateConfiguration")
        );

        assertEquals("DB_ARENA_JWT_SECRET must be configured outside local development.", exception.getMessage());
    }

    @Test
    void generatedTokenShouldValidateWhenSecretAndExpirationAreConfigured() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", "test-secret-key");
        ReflectionTestUtils.setField(jwtUtil, "EXPIRATION_TIME", 60_000L);
        ReflectionTestUtils.setField(jwtUtil, "datasourceUrl", "jdbc:postgresql://localhost:5432/db-arena");
        ReflectionTestUtils.setField(jwtUtil, "redisHost", "localhost");
        ReflectionTestUtils.setField(jwtUtil, "frontendOrigin", "http://localhost:4200");
        ReflectionTestUtils.invokeMethod(jwtUtil, "validateConfiguration");

        String token = jwtUtil.generateToken("goku");

        assertNotNull(token);
        assertEquals("goku", jwtUtil.validateToken(token));
    }
}
