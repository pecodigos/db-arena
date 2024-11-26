package com.pecodigos.dbarena.abilities.entity;

import com.pecodigos.dbarena.characters.entity.Character;
import com.pecodigos.dbarena.enums.EnergyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "abilities")
public class Ability implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private EnergyType energyType;

    @ManyToOne
    private Character character;
}
