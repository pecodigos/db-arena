package com.pecodigos.dbarena.ingame.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pecodigos.dbarena.ingame.entities.Character;
import com.pecodigos.dbarena.ingame.repositories.CharacterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

@Service
@AllArgsConstructor
public class CharacterInitService {

    private CharacterRepository characterRepository;

    public void importCharacters(String folderPath) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            File folder = new File(folderPath);
            File[] files = Objects.requireNonNull(folder.listFiles((dir, name) -> name.endsWith(".json")));

            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                if (file.isFile()) {
                    var character = objectMapper.readValue(file, Character.class);

                    boolean exists = characterRepository.findByName(character.getName()).isPresent();
                    if (exists) continue;

                    character.getAbilities().forEach(ability -> {
                        ability.setCharacter(character);
                        ability.getCost().forEach(cost -> cost.setAbility(ability));
                    });

                    characterRepository.save(character);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + e.getMessage());
        }
    }
}
