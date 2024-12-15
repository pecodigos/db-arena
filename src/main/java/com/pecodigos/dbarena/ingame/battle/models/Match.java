package com.pecodigos.dbarena.ingame.battle.models;

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

    public Player whoStarts() {
        return Math.random() < 0.5 ? playerOne : playerTwo;
    }

    public void switchTurn() {
        this.currentPlayer = (this.currentPlayer == playerOne) ? playerTwo : playerOne;
    }
}
