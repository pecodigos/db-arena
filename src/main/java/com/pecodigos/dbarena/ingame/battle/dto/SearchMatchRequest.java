package com.pecodigos.dbarena.ingame.battle.dto;

import com.pecodigos.dbarena.ingame.battle.models.Fighter;
import com.pecodigos.dbarena.ingame.enums.battle.BattleQueueType;

public record SearchMatchRequest(Fighter[] team, BattleQueueType battleQueueType) {
}
