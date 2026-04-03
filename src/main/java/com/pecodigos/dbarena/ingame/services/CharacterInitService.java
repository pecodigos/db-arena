package com.pecodigos.dbarena.ingame.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pecodigos.dbarena.ingame.entities.Character;
import com.pecodigos.dbarena.ingame.enums.skills.EffectType;
import com.pecodigos.dbarena.ingame.repositories.CharacterRepository;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

@Service
@AllArgsConstructor
public class CharacterInitService {

    private static final String EARLY_KRILLIN_NAME = "Early Krillin";
    private static final String EARLY_PICCOLO_NAME = "Early Piccolo";
    private static final String EARLY_VEGETA_NAME = "Early Vegeta";
    private static final String DESTRUCTO_DISC_NAME = "Destructo Disc";
    private static final String EVADE_NAME = "Evade";
    private static final String PROTECT_ALLY_NAME = "Protect an Ally";
    private static final String GALICK_GUN_NAME = "Galick Gun";
    private static final String EXPLOSION_WAVE_NAME = "Explosion Wave";

    private CharacterRepository characterRepository;

    public void importCharacters(String resourcePattern) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(resourcePattern);

            Arrays.sort(resources, Comparator.comparing(Resource::getFilename, Comparator.nullsLast(String::compareTo)));
            for (Resource resource : resources) {
                if (!resource.exists()) {
                    continue;
                }

                var character = objectMapper.readValue(resource.getInputStream(), Character.class);

                boolean exists = characterRepository.findByName(character.getName()).isPresent();
                if (exists) continue;

                character.getAbilities().forEach(ability -> {
                    ability.setCharacter(character);
                    ability.getCost().forEach(cost -> cost.setAbility(ability));
                });

                characterRepository.save(character);
            }

            applyKnownCharacterHotfixes();
        } catch (IOException e) {
            System.err.println("Error reading file " + e.getMessage());
        }
    }

    private void applyKnownCharacterHotfixes() {
        characterRepository.findWithAbilitiesByName(EARLY_KRILLIN_NAME).ifPresent(character -> {
            boolean updated = false;

            if (character.getAbilities() != null) {
                for (var ability : character.getAbilities()) {
                    if (DESTRUCTO_DISC_NAME.equals(ability.getName()) && !Boolean.TRUE.equals(ability.getIsHarmful())) {
                        ability.setIsHarmful(true);
                        updated = true;
                    }

                    if (EVADE_NAME.equals(ability.getName()) && ability.getEffectType() != EffectType.INVULNERABLE) {
                        ability.setEffectType(EffectType.INVULNERABLE);
                        updated = true;
                    }
                }
            }

            if (updated) {
                characterRepository.save(character);
            }
        });

        characterRepository.findWithAbilitiesByName(EARLY_PICCOLO_NAME).ifPresent(character -> {
            boolean updated = false;

            if (character.getAbilities() != null) {
                for (var ability : character.getAbilities()) {
                    if (PROTECT_ALLY_NAME.equals(ability.getName()) && ability.getDurationInTurns() != 1) {
                        ability.setDurationInTurns(1);
                        updated = true;
                    }
                }
            }

            if (updated) {
                characterRepository.save(character);
            }
        });

        characterRepository.findWithAbilitiesByName(EARLY_VEGETA_NAME).ifPresent(character -> {
            boolean updated = false;

            if (character.getAbilities() != null) {
                for (var ability : character.getAbilities()) {
                    if (EXPLOSION_WAVE_NAME.equals(ability.getName()) && !Boolean.TRUE.equals(ability.getIsHarmful())) {
                        ability.setIsHarmful(true);
                        updated = true;
                    }

                    if (GALICK_GUN_NAME.equals(ability.getName())
                            && (ability.getSecondaryDamage() == null || ability.getSecondaryDamage() != 10)) {
                        ability.setSecondaryDamage(10);
                        updated = true;
                    }
                }
            }

            if (updated) {
                characterRepository.save(character);
            }
        });
    }
}
