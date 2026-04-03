package com.pecodigos.dbarena.ingame.battle.dto;

import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import java.util.List;
import java.util.Map;

public record TurnAction(int characterIndex, int skillIndex, List<Integer> targetIndexes, Map<EnergyType, Integer> anyEnergyChoices) {
}
