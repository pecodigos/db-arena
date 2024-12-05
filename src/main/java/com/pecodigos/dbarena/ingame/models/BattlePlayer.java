package com.pecodigos.dbarena.ingame.models;

import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BattlePlayer {
    private String username;
    private List<BattleFighter> team;
    private List<BattleFighter> livingFighters;
    private Map<EnergyType, Integer> energy;
    private boolean isActiveTurn;

    public BattlePlayer(List<BattleFighter> team) {
        this.team = team;
        this.livingFighters = team;
        this.energy = new HashMap<>();
        initializeEnergy();
    }

    private void initializeEnergy() {
        for (EnergyType energyType : EnergyType.values()) {
            this.energy.put(energyType, 0);
        }
    }
}
