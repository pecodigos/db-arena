package com.pecodigos.dbarena.ingame.dtos;

import com.pecodigos.dbarena.ingame.entities.Ability;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FighterDTO(@NotBlank @NotNull Long id,
                         @NotBlank @NotNull String name,
                         @NotBlank @NotNull String description,
                         @NotBlank @NotNull String imagePath,
                         @NotNull List<Ability> abilities,
                         @NotNull int currentHp,
                         @NotNull int currentDef,
                         @NotNull int MAX_HP,
                         @NotNull boolean isStunned,
                         @NotNull boolean isAlive) {
}
