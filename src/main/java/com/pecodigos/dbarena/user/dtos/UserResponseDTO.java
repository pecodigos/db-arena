package com.pecodigos.dbarena.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserResponseDTO(@NotBlank @NotNull String username, @NotBlank @NotNull String email) {
}
