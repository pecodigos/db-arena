package com.pecodigos.dbarena.ingame.entities;

import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import jakarta.persistence.*;

@Entity
@Table(name = "ability_costs")
public class AbilityCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ability_id")
    private Ability ability;

    @Enumerated(EnumType.STRING)
    private EnergyType energyType;

    private Integer cost;
}
