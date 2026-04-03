package com.pecodigos.dbarena.user.service;

import com.pecodigos.dbarena.exceptions.UserAlreadyExistsException;
import com.pecodigos.dbarena.user.dtos.UserRequestDTO;
import com.pecodigos.dbarena.user.dtos.mapper.UserMapper;
import com.pecodigos.dbarena.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class UserServiceCreateSecurityTests {

    @Test
    void createShouldTranslateDatabaseUniquenessViolations() {
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        UserService userService = new UserService(userMapper, userRepository, passwordEncoder);

        Mockito.when(userRepository.findByUsername("goku")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail("goku@example.com")).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        Mockito.when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.create(request("goku", "goku@example.com", "secret123"))
        );

        assertEquals("Username or email already taken.", exception.getMessage());
    }

    private static UserRequestDTO request(String username, String email, String password) {
        return new UserRequestDTO(
                null,
                username,
                email,
                password,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
