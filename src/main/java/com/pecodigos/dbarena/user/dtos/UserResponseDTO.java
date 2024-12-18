package com.pecodigos.dbarena.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserResponseDTO(@NotNull UUID id, @NotBlank @NotNull String username, @NotBlank @NotNull String email) {
}
