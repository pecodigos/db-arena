package com.pecodigos.dbarena.characters.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CharacterRequestDTO(@NotNull Long id, @NotBlank @NotNull String name, @NotBlank @NotNull String description) {
}
