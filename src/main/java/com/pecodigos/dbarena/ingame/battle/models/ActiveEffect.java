package com.pecodigos.dbarena.ingame.battle.models;

import com.pecodigos.dbarena.ingame.enums.skills.DamageType;
import com.pecodigos.dbarena.ingame.enums.skills.EffectType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActiveEffect {
    private String name;
    private String description;
    private String imagePath;
    private Integer remainingTurns;
    private boolean isHarmful;
    private boolean invisible;
    private String casterUsername;
    private String casterCharacterName;
    private EffectType effectType;
    private DamageType damageType;
    private Integer damage;

    public ActiveEffect(
            String name,
            String description,
            String imagePath,
            Integer remainingTurns,
            boolean isHarmful,
            boolean invisible,
            String casterUsername
    ) {
        this(name, description, imagePath, remainingTurns, isHarmful, invisible, casterUsername, null, null, null, null);
    }

    public ActiveEffect(
            String name,
            String description,
            String imagePath,
            Integer remainingTurns,
            boolean isHarmful,
            boolean invisible,
            String casterUsername,
            String casterCharacterName,
            EffectType effectType,
            DamageType damageType,
            Integer damage
    ) {
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.remainingTurns = remainingTurns;
        this.isHarmful = isHarmful;
        this.invisible = invisible;
        this.casterUsername = casterUsername;
        this.casterCharacterName = casterCharacterName;
        this.effectType = effectType;
        this.damageType = damageType;
        this.damage = damage;
    }
}
