package com.pecodigos.dbarena.characters.dtos.mapper;

import com.pecodigos.dbarena.characters.dtos.CharacterRequestDTO;
import com.pecodigos.dbarena.characters.dtos.CharacterResponseDTO;
import com.pecodigos.dbarena.characters.entity.Character;
import org.mapstruct.Mapper;

@Mapper
public interface CharacterMapper {
    CharacterResponseDTO toResponseDto(Character character);
    CharacterRequestDTO toRequestDto(Character character);
}
