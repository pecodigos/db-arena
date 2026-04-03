package com.pecodigos.dbarena.ingame.battle.dto.mapper;

import com.pecodigos.dbarena.ingame.battle.dto.MatchDTO;
import com.pecodigos.dbarena.ingame.battle.models.Match;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MatchMapper {
    @Mapping(target = "playerOneName", source = "playerOne.username")
    @Mapping(target = "playerTwoName", source = "playerTwo.username")
    @Mapping(target = "currentPlayerName", source = "currentPlayer.username")
    MatchDTO toDTO(Match match);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "turnNumber", source = "turnNumber")
    @Mapping(target = "battleState", source = "battleState")
    Match toEntity(MatchDTO matchDTO);
}
