//package com.pecodigos.dbarena.ingame.services;
//
//import com.pecodigos.dbarena.exceptions.IllegalMoveException;
//import com.pecodigos.dbarena.ingame.entities.Ability;
//import com.pecodigos.dbarena.ingame.models.BattleRoom;
//import com.pecodigos.dbarena.ingame.fighters.model.Fighter;
//import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
//import com.pecodigos.dbarena.ingame.models.BattlePlayer;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.UUID;
//
//@Service
//@AllArgsConstructor
//public class BattleService {
//
//    private final RedisBattleService redisBattleService;
//
//    public BattleRoom startMatch(BattlePlayer playerOne, BattlePlayer playerTwo) {
//        BattleRoom battleRoom = BattleRoom.builder()
//                .id(UUID.randomUUID())
//                .playerOne(playerOne)
//                .playerTwo(playerTwo)
//                .isPlayerOneTurn(flipCoin())
//                .battleState(BattleState.OPPONENT_FOUND)
//                .build();
//
//        redisBattleService.saveBattleState(battleRoom.getId().toString(), battleRoom);
//
//        return battleRoom;
//    }
//
//    public boolean flipCoin() {
//        return Math.random() < 0.5;
//    }
//
//    public void useOffensiveAbility(String gameId, Ability ability, Fighter attacker, Fighter target) {
//        var battleRoom = redisBattleService.getBattleState(gameId);
//
//        if (battleRoom.getBattleState() == BattleState.GAME_OVER) {
//            throw new IllegalMoveException("Game is already over.");
//        }
//
//        var activePlayer = battleRoom.getActivePlayer();
//        var attack = activePlayer.findCharacter(attacker.getId());
//
//        ability.getCost().forEach((type, amount) -> {
//            if (activePlayer.getEnergy(type) < amount) {
//                throw new IllegalMoveException("Not enough energy to use this skill");
//            }
//        });
//        ability.getCost().forEach(activePlayer::subtractEnergy);
//
//        var damage = ability.getDamage();
//        target.setCurrentHp(target.getCurrentHp() - Math.max(0, damage - target.getCurrentDef()));
//
//        if (target.getCurrentHp() <= 0) {
//            battleRoom.getInactivePlayer().killCharacter(target);
//        }
//
//        checkGameOver(battleRoom);
//
//        redisBattleService.saveBattleState(gameId, battleRoom);
//    }
//
//
//    public void checkGameOver(BattleRoom battleRoom) {
//        boolean isPlayerOneDead = battleRoom.getPlayerOne().getLivingFighters().isEmpty();
//        boolean isPlayerTwoDead = battleRoom.getPlayerTwo().getLivingFighters().isEmpty();
//
//        if (isPlayerOneDead || isPlayerTwoDead) {
//            battleRoom.setBattleState(BattleState.GAME_OVER);
//        }
//    }
//
//    public void endTurn(String gameId) {
//        var battleRoom = redisBattleService.getBattleState(gameId);
//
//        battleRoom.getActivePlayer().addEnergy();
//        switchTurn(gameId);
//
//        redisBattleService.saveBattleState(gameId, battleRoom);
//    }
//
//    public void switchTurn(String gameId) {
//        var battleRoom = redisBattleService.getBattleState(gameId);
//
//        if (battleRoom.getBattleState() == BattleState.PLAYER_ONE_TURN) {
//            battleRoom.setBattleState(BattleState.PLAYER_TWO_TURN);
//        } else {
//            battleRoom.setBattleState(BattleState.PLAYER_ONE_TURN);
//        }
//    }
//}
