package com.pecodigos.dbarena.ingame.battle.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Match {
    private Player playerOne;
    private Player playerTwo;
    private Player currentPlayer;
    private Integer turnNumber;

    public Player whoStarts() {
        if (Math.random() > 0.5) {
            return this.playerOne;
        } else {
            return this.playerTwo;
        }
    }
}
