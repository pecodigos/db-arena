package com.pecodigos.dbarena.ingame.battle.dto.mapper;

import com.pecodigos.dbarena.ingame.battle.dto.MatchLogDTO;
import com.pecodigos.dbarena.ingame.entities.MatchLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface MatchLogMapper {
    MatchLog toEntity(MatchLogDTO matchLogDTO);
    MatchLogDTO toDTO(MatchLog matchLog);
}
