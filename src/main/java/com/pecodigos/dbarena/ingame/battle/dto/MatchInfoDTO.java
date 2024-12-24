package com.pecodigos.dbarena.ingame.battle.dto;

import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;

public record MatchInfoDTO(Match match, PublicProfileDTO opponentData) {
}
