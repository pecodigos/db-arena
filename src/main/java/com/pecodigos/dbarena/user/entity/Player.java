package com.pecodigos.dbarena.user.entity;

import com.pecodigos.dbarena.ingame.entities.Fighter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    private User user;

    private Integer currentLevel;
    private Integer highestLevel;
    private Long currentExp;
    private Integer winCount;
    private Integer loseCount;
    private Integer currentStreak;
    private Integer highestStreak;

    @OneToMany
    private List<Fighter> unlockedFighters;
}
