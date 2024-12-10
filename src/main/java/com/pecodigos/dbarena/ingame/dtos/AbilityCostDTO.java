package com.pecodigos.dbarena.ingame.dtos;

import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import jakarta.validation.constraints.NotNull;

public record AbilityCostDTO(@NotNull EnergyType energyType,
                             @NotNull Integer amount) {
}
