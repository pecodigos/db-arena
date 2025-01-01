package com.pecodigos.dbarena.ingame.battle.dto.mapper;

import com.pecodigos.dbarena.ingame.battle.dto.MatchLogDTO;
import com.pecodigos.dbarena.ingame.entities.MatchLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MatchLogMapper {
    MatchLog toEntity(MatchLogDTO matchLogDTO);
    MatchLogDTO toDTO(MatchLog matchLog);
}
