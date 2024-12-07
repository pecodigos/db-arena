package com.pecodigos.dbarena.ingame.entities;

import com.pecodigos.dbarena.ingame.enums.skills.Distance;
import com.pecodigos.dbarena.ingame.enums.skills.PersistentType;
import com.pecodigos.dbarena.ingame.enums.skills.SkillType;
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
    private Integer damage;
    private Integer cooldown;
    private Integer damageReductionAmount;

    @OneToMany(mappedBy = "ability", cascade = CascadeType.ALL)
    private List<AbilityCost> cost;

    private Distance distance;
    private SkillType skillType;
    private PersistentType persistentType;
    private Integer durationInTurns;
    private boolean isUnique;
    private boolean isHarmful;
    private boolean isActive;

    @ManyToMany
    @JoinTable(
            name = "ability_requirements",
            joinColumns = @JoinColumn(name = "ability_id"),
            inverseJoinColumns = @JoinColumn(name = "required_ability_id")
    )
    private List<Ability> requirements;

    @ManyToOne
    @JoinColumn(name = "fighter_id")
    private Character character;
}
