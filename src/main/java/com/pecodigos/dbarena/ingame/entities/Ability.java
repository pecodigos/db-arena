package com.pecodigos.dbarena.ingame.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pecodigos.dbarena.ingame.enums.skills.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "abilities")
public class Ability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String imagePath;
    private Integer damage;
    private Integer cooldown;

    @OneToMany(mappedBy = "ability", cascade = CascadeType.ALL)
    private List<AbilityCost> cost;

    @Enumerated(EnumType.STRING)
    private DamageType damageType;

    @Enumerated(EnumType.STRING)
    private EffectType effectType;

    @Enumerated(EnumType.STRING)
    private Distance distance;

    @Enumerated(EnumType.STRING)
    private SkillType skillType;

    @Enumerated(EnumType.STRING)
    private PersistentType persistentType;

    private Integer durationInTurns;

    @JsonProperty("isUnique")
    private Boolean isUnique;

    @JsonProperty("isHarmful")
    private Boolean isHarmful;

    @ManyToMany
    @JoinTable(
            name = "ability_requirements",
            joinColumns = @JoinColumn(name = "ability_id"),
            inverseJoinColumns = @JoinColumn(name = "required_ability_id")
    )
    private List<Ability> requirements;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;
}
