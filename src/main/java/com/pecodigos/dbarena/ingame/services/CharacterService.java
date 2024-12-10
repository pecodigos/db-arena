package com.pecodigos.dbarena.ingame.services;

import com.pecodigos.dbarena.exceptions.FighterNotFoundException;
import com.pecodigos.dbarena.ingame.dtos.CharacterDTO;
import com.pecodigos.dbarena.ingame.dtos.mappers.CharacterMapper;
import com.pecodigos.dbarena.ingame.repositories.CharacterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;

    private final CharacterMapper characterMapper;

    public List<CharacterDTO> getAllCharacters() {
        return characterRepository.findAll()
                .stream()
                .map(characterMapper::toCharacterDTO)
                .toList();
    }

    public CharacterDTO getCharacter(Long id) {
        return characterMapper.toCharacterDTO(
                characterRepository.findById(id).orElseThrow(() -> new FighterNotFoundException("Character not found.")));
    }
}
