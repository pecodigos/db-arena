package com.pecodigos.dbarena.user.repository;

import com.pecodigos.dbarena.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryConstraintsTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveShouldRejectDuplicateUsername() {
        userRepository.saveAndFlush(user("goku", "goku@example.com"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(user("goku", "vegeta@example.com"))
        );
    }

    @Test
    void saveShouldRejectDuplicateEmail() {
        userRepository.saveAndFlush(user("goku", "goku@example.com"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(user("vegeta", "goku@example.com"))
        );
    }

    private static User user(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .password("encoded-password")
                .build();
    }
}
