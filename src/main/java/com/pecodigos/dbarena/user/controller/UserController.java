package com.pecodigos.dbarena.user.controller;

import com.pecodigos.dbarena.user.dtos.UserRequestDTO;
import com.pecodigos.dbarena.user.dtos.UserResponseDTO;
import com.pecodigos.dbarena.user.service.UserService;
import lombok.AllArgsConstructor;
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

    @GetMapping("/")
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        return ResponseEntity.ok(userService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable(name = "id") UUID id) {
        return ResponseEntity.ok(userService.find(id));
    }


}
