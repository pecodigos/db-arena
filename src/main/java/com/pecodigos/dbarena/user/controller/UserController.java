package com.pecodigos.dbarena.user.controller;

import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.dtos.UserRequestDTO;
import com.pecodigos.dbarena.user.dtos.UserResponseDTO;
import com.pecodigos.dbarena.user.service.UserService;
import lombok.AllArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    @GetMapping("/profile/{username}")
    public ResponseEntity<PublicProfileDTO> getPublicProfile(@PathVariable String username) {
        return ResponseEntity.ok(userService.getPublicProfile(username));
    }
}
