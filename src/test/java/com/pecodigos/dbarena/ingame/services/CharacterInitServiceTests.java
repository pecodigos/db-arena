package com.pecodigos.dbarena.ingame.services;

import com.pecodigos.dbarena.ingame.entities.Ability;
import com.pecodigos.dbarena.ingame.entities.Character;
import com.pecodigos.dbarena.ingame.enums.skills.EffectType;
import com.pecodigos.dbarena.ingame.repositories.CharacterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CharacterInitServiceTests {

    @Mock
    private CharacterRepository characterRepository;

    @InjectMocks
    private CharacterInitService characterInitService;

    @Test
    void importCharactersShouldHotfixKrillinDestructoDiscAsHarmful() {
        Ability destructoDisc = Ability.builder()
                .name("Destructo Disc")
                .isHarmful(false)
                .build();

        Ability evade = Ability.builder()
                .name("Evade")
                .effectType(EffectType.NONE)
                .build();

        Character piccolo = Character.builder()
                .name("Early Piccolo")
                .abilities(new ArrayList<>(List.of(
                        Ability.builder().name("Protect an Ally").durationInTurns(2).build()
                )))
                .build();

        Character vegeta = Character.builder()
                .name("Early Vegeta")
                .abilities(new ArrayList<>(List.of(
                        Ability.builder().name("Galick Gun").secondaryDamage(null).build(),
                        Ability.builder().name("Explosion Wave").isHarmful(false).build()
                )))
                .build();

        Character krillin = Character.builder()
                .name("Early Krillin")
                .abilities(new ArrayList<>(List.of(destructoDisc, evade)))
                .build();

        when(characterRepository.findWithAbilitiesByName("Early Krillin")).thenReturn(Optional.of(krillin));
        when(characterRepository.findWithAbilitiesByName("Early Piccolo")).thenReturn(Optional.of(piccolo));
        when(characterRepository.findWithAbilitiesByName("Early Vegeta")).thenReturn(Optional.of(vegeta));

        characterInitService.importCharacters("classpath*:characters/does-not-exist/*.json");

        assertTrue(Boolean.TRUE.equals(destructoDisc.getIsHarmful()));
        assertTrue(evade.getEffectType() == EffectType.INVULNERABLE);
        assertEquals(1, piccolo.getAbilities().get(0).getDurationInTurns());
        assertEquals(10, vegeta.getAbilities().get(0).getSecondaryDamage());
        assertTrue(Boolean.TRUE.equals(vegeta.getAbilities().get(1).getIsHarmful()));
        verify(characterRepository).save(krillin);
        verify(characterRepository).save(piccolo);
        verify(characterRepository).save(vegeta);
    }

    @Test
    void importCharactersShouldSkipSaveWhenKrillinHotfixAlreadyApplied() {
        Ability destructoDisc = Ability.builder()
                .name("Destructo Disc")
                .isHarmful(true)
                .build();

        Ability evade = Ability.builder()
                .name("Evade")
                .effectType(EffectType.INVULNERABLE)
                .build();

        Character piccolo = Character.builder()
                .name("Early Piccolo")
                .abilities(new ArrayList<>(List.of(
                        Ability.builder().name("Protect an Ally").durationInTurns(1).build()
                )))
                .build();

        Character vegeta = Character.builder()
                .name("Early Vegeta")
                .abilities(new ArrayList<>(List.of(
                        Ability.builder().name("Galick Gun").secondaryDamage(10).build(),
                        Ability.builder().name("Explosion Wave").isHarmful(true).build()
                )))
                .build();

        Character krillin = Character.builder()
                .name("Early Krillin")
                .abilities(new ArrayList<>(List.of(destructoDisc, evade)))
                .build();

        when(characterRepository.findWithAbilitiesByName("Early Krillin")).thenReturn(Optional.of(krillin));
        when(characterRepository.findWithAbilitiesByName("Early Piccolo")).thenReturn(Optional.of(piccolo));
        when(characterRepository.findWithAbilitiesByName("Early Vegeta")).thenReturn(Optional.of(vegeta));

        characterInitService.importCharacters("classpath*:characters/does-not-exist/*.json");

        verify(characterRepository, never()).save(krillin);
        verify(characterRepository, never()).save(piccolo);
        verify(characterRepository, never()).save(vegeta);
    }
}
