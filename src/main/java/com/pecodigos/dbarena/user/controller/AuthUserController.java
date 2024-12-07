package com.pecodigos.dbarena.user.controller;

import com.pecodigos.dbarena.config.auth.JwtUtil;
import com.pecodigos.dbarena.user.dtos.PasswordDTO;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.dtos.UserRequestDTO;
import com.pecodigos.dbarena.user.dtos.UserResponseDTO;
import com.pecodigos.dbarena.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthUserController {

    private UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            var loggedInUser = userService.login(userRequestDTO);
            String token = jwtUtil.generateToken(loggedInUser.username());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", new UserResponseDTO(loggedInUser.id(), loggedInUser.username(), loggedInUser.email()));

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(userRequestDTO));
    }

    @PutMapping("/user/{id}/password")
    public ResponseEntity<UserResponseDTO> updatePassword(@PathVariable UUID id, @RequestBody PasswordDTO passwordDTO) {
        return ResponseEntity.ok(userService.changePassword(id, passwordDTO.currentPassword(), passwordDTO.currentPassword()));
    }

    @PutMapping("/user/{id}/avatar")
    public ResponseEntity<UserResponseDTO> updateAvatar(@PathVariable UUID id, @RequestBody PublicProfileDTO publicProfileDTO) {
        return ResponseEntity.ok(userService.changeAvatar(id, publicProfileDTO.profilePicturePath()));
    }
}
