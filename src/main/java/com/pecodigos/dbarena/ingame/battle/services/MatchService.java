//package com.pecodigos.dbarena.ingame.battle.services;
//
//import com.pecodigos.dbarena.ingame.battle.models.Match;
//import com.pecodigos.dbarena.ingame.battle.models.Player;
//import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.security.core.parameters.P;
//import org.springframework.stereotype.Service;
//
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//
//@Service
//public class MatchService {
//
//    private final Set<String> waitingPlayers = new HashSet<>();
//    private final SimpMessagingTemplate messagingTemplate;
//    private Match currentMatch;
//
//    public MatchService(SimpMessagingTemplate messagingTemplate) {
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    public void searchForMatch(String playerId, Player player) {
//        waitingPlayers.add(playerId);
//        tryToMatch(player);
//    }
//
//    private void tryToMatch(Player player) {
//        if (waitingPlayers.size() == 2) {
//            Iterator<String> iterator = waitingPlayers.iterator();
//            String playerOneId = iterator.next();
//            String playerTwoId = iterator.next();
//
//            sendMatchStart(currentMatch);
//        }
//    }
//
//    private void sendMatchStart(Match match) {
//        System.out.println();
//    }
//}
