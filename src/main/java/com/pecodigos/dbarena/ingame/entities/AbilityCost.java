package com.pecodigos.dbarena.ingame.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pecodigos.dbarena.ingame.entities.Ability;
import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ability_costs")
public class AbilityCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EnergyType energyType;

    private Integer amount;

    @ManyToOne
    @JoinColumn(name = "ability_id", nullable = false)
    @JsonBackReference
    private Ability ability;
}
