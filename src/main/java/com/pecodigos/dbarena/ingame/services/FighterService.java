package com.pecodigos.dbarena.ingame.services;

import com.pecodigos.dbarena.exceptions.FighterNotFoundException;
import com.pecodigos.dbarena.ingame.dtos.FighterDTO;
import com.pecodigos.dbarena.ingame.entities.Character;
import com.pecodigos.dbarena.ingame.repositories.FighterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FighterService {

    private FighterRepository fighterRepository;

    public Character getCharacter(FighterDTO fighterDTO) {
        return fighterRepository.findById(fighterDTO.id()).orElseThrow();
    }

    public List<Character> getAllCharacters() {
        return fighterRepository.findAll();
    }

    public Character create(FighterDTO fighterDTO) {
        return Character.builder()
                .name(fighterDTO.name())
                .description(fighterDTO.description())
                .imagePath(fighterDTO.imagePath())
                .abilities(fighterDTO.abilities())
                .build();
    }

    public Character update(Long id, FighterDTO fighterDTO) {
        var optionalFighter = fighterRepository.findById(id);

        if (optionalFighter.isEmpty()) {
            throw new FighterNotFoundException("Character not found with that id.");
        }

        var fighter = optionalFighter.get();

        fighter.setName(fighterDTO.name());
        fighter.setDescription(fighterDTO.description());
        fighter.setAbilities(fighterDTO.abilities());

        fighterRepository.save(fighter);

        return fighter;
    }
}
