package com.pecodigos.dbarena.ingame.battle.dto.mapper;

import com.pecodigos.dbarena.ingame.battle.dto.MatchDTO;
import com.pecodigos.dbarena.ingame.battle.models.Match;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MatchMapper {
    MatchDTO toDTO(Match match);
    Match toEntity(MatchDTO matchDTO);
}
