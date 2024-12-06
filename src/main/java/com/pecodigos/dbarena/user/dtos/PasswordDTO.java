package com.pecodigos.dbarena.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasswordDTO(@NotBlank @NotNull String currentPassword, @NotBlank @NotNull String newPassword) {
}
