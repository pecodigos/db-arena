package com.pecodigos.dbarena.ingame.battle.services;

import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.ingame.battle.models.Player;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    private static final short FIRST_TURN_ENERGY_AMOUNT = 1;
    private static final short SECOND_TURN_ENERGY_AMOUNT = 3;

    public Match createMatch(Player playerOne, Player playerTwo) {
        if (playerOne == null || playerTwo == null) {
            throw new IllegalArgumentException("Players cannot be null");
        }

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .turnNumber(1)
                .build();

        var startingPlayer = match.whoStarts();
        match.setCurrentPlayer(startingPlayer);

        playerOne.generateEnergy();
        playerTwo.generateEnergy();

        return match;
    }

    public void endTurn(Match match) {
        var currentPlayer = match.getCurrentPlayer();

        currentPlayer.generateEnergy();

        match.switchTurn();
        match.setTurnNumber(match.getTurnNumber() + 1);
    }
}
