package com.pecodigos.dbarena.ingame.battle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pecodigos.dbarena.ingame.enums.battle.BattleQueueType;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;

import java.time.LocalDateTime;

public record MatchLogDTO(Long id, PublicProfileDTO playerOne, PublicProfileDTO playerTwo, PublicProfileDTO winner, BattleQueueType battleQueueType, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime battleDate) {
}
