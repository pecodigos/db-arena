package com.pecodigos.dbarena.ingame.battle.models;

import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Match {
    private Player playerOne;
    private Player playerTwo;
    private Player currentPlayer;
    private Integer turnNumber;
    private BattleState battleState;

    public Player whoStarts() {
        return Math.random() < 0.5 ? playerOne : playerTwo;
    }

    public void endTurn() {
        this.currentPlayer.generateEnergy();
        this.currentPlayer.reduceCooldowns();
        this.currentPlayer = (this.currentPlayer == playerOne) ? playerTwo : playerOne;
    }
}
