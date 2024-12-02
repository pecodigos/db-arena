package com.pecodigos.dbarena.characters.dtos;

import com.pecodigos.dbarena.enums.EnergyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CharacterResponseDTO(@NotBlank @NotNull String name, @NotBlank @NotNull String description, @NotNull EnergyType energyType) {
}
