package com.pecodigos.dbarena.ingame.characters.model;

import com.pecodigos.dbarena.ingame.abilities.models.Ability;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Character {
    private String name;
    private String description;
    private List<Ability> abilityNames;
}
