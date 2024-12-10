package com.pecodigos.dbarena.ingame.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pecodigos.dbarena.ingame.enums.skills.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AbilityDTO(
        @NotNull Long id,
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String imagePath,
        @NotNull Integer damage,
        @NotNull Integer cooldown,
        @NotNull DamageType damageType,
        @NotNull EffectType effectType,
        @NotNull Distance distance,
        @NotNull SkillType skillType,
        @NotNull PersistentType persistentType,
        @NotNull Integer durationInTurns,
        @JsonProperty("isUnique") Boolean isUnique,
        @JsonProperty("isHarmful") Boolean isHarmful,
        @NotNull List<AbilityCostDTO> cost
) {}
