package com.pecodigos.dbarena.ingame.battle.dto;

import com.pecodigos.dbarena.ingame.enums.battle.BattleState;

public record MatchDTO(String playerOneName, String playerTwoName, String currentPlayerName, Integer turnNumber, BattleState battleState) {
}
