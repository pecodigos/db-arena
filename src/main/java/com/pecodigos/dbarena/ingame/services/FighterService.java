package com.pecodigos.dbarena.ingame.services;

import com.pecodigos.dbarena.exceptions.FighterNotFoundException;
import com.pecodigos.dbarena.ingame.dtos.FighterDTO;
import com.pecodigos.dbarena.ingame.entities.Fighter;
import com.pecodigos.dbarena.ingame.repositories.FighterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FighterService {

    private FighterRepository fighterRepository;

    public Fighter getCharacter(FighterDTO fighterDTO) {
        return fighterRepository.findById(fighterDTO.id()).orElseThrow();
    }

    public List<Fighter> getAllCharacters() {
        return fighterRepository.findAll();
    }

    public Fighter create(FighterDTO fighterDTO) {
        return Fighter.builder()
                .name(fighterDTO.name())
                .description(fighterDTO.description())
                .imagePath(fighterDTO.imagePath())
                .abilities(fighterDTO.abilities())
                .build();
    }

    public Fighter update(Long id, FighterDTO fighterDTO) {
        var optionalFighter = fighterRepository.findById(id);

        if (optionalFighter.isEmpty()) {
            throw new FighterNotFoundException("Fighter not found with that id.");
        }

        var fighter = optionalFighter.get();

        fighter.setName(fighterDTO.name());
        fighter.setDescription(fighterDTO.description());
        fighter.setAbilities(fighterDTO.abilities());

        fighterRepository.save(fighter);

        return fighter;
    }
}
