package com.pecodigos.dbarena.users.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserRequestDTO(@NotNull UUID id, @NotBlank @NotNull String name, @NotBlank @NotNull String username, @NotBlank @NotNull String email, @NotBlank @NotNull String password){
}
