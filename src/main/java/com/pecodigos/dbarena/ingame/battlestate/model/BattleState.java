package com.pecodigos.dbarena.ingame.battlestate.model;

import com.pecodigos.dbarena.ingame.enums.EnergyType;
import com.pecodigos.dbarena.ingame.player.model.Player;

import java.util.List;
import java.util.Map;

public class BattleState {

    private Player playerOne;
    private Player playerTwo;

    private List<Character> playerOneActiveCharacters;
    private List<Character> playerTwoActiveCharacters;

    private Map<EnergyType, Integer> playerOneEnergy;
    private Map<EnergyType, Integer> playerTwoEnergy;

    private boolean isPlayerOneTurn;
    private boolean isGameOver;
}
