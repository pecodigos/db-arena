package com.pecodigos.dbarena.ingame.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CharacterDTO(@NotNull Long id,
                           @NotBlank @NotNull String name,
                           @NotBlank @NotNull String description,
                           @NotBlank @NotNull String imagePath,
                           @NotNull List<AbilityDTO> abilities) {
}
