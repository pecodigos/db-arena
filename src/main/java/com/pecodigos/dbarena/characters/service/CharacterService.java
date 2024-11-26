package com.pecodigos.dbarena.characters.service;

import com.pecodigos.dbarena.characters.dtos.CharacterResponseDTO;
import com.pecodigos.dbarena.characters.dtos.mapper.CharacterMapper;
import com.pecodigos.dbarena.characters.repository.CharacterRepository;
import com.pecodigos.dbarena.exceptions.CharacterNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CharacterService {

    private CharacterRepository characterRepository;
    private CharacterMapper characterMapper;

    public CharacterResponseDTO getCharacterById(Long id) {
        return characterMapper.toResponseDto(characterRepository.findById(id)
                .orElseThrow(() -> new CharacterNotFoundException("Character not found with that ID.")));
    }

    public List<CharacterResponseDTO> list() {
        return characterRepository.findAll()
                .stream()
                .map(characterMapper::toResponseDto)
                .toList();
    }
}
