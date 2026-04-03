package com.pecodigos.dbarena.ingame.battle.dto;

import com.pecodigos.dbarena.ingame.enums.battle.BattleQueueType;

import java.util.List;

public record SearchMatchRequest(List<Long> characterIds, BattleQueueType battleQueueType) {
}
