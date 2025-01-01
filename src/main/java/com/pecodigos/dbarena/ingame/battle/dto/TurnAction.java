package com.pecodigos.dbarena.ingame.battle.dto;

import java.util.List;

public record TurnAction(int characterIndex, int skillIndex, List<Integer> targetIndexes) {
}
