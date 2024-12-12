package com.pecodigos.dbarena.ingame.battle.services;

import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.ingame.battle.models.Player;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    public Match createMatch(Player playerOne, Player playerTwo) {
        Match match = new Match();
        match.setPlayerOne(playerOne);
        match.setPlayerTwo(playerTwo);
        match.setCurrentPlayer(match.whoStarts());

        var currentPlayer = match.getCurrentPlayer();
        currentPlayer.generateEnergy();

        return match;
    }
}
