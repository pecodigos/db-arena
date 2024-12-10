package com.pecodigos.dbarena.ingame.dtos.mappers;

import com.pecodigos.dbarena.ingame.dtos.CharacterDTO;
import com.pecodigos.dbarena.ingame.entities.Character;
import org.mapstruct.Mapper;

@Mapper
public interface CharacterMapper {
    CharacterDTO toCharacterDTO(Character character);
}
