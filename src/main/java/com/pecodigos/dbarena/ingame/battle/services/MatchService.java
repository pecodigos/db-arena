package com.pecodigos.dbarena.ingame.battle.services;

import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.ingame.battle.models.Player;
import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class MatchService {

    private static final String QUEUE_MATCH = "/queue/match";

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    private final Queue<PublicProfileDTO> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final Map<String, Match> activeMatches = new ConcurrentHashMap<>();

    public void searchForMatch(String username) {
        PublicProfileDTO playerProfile = userService.getPublicProfile(username);

        waitingPlayers.add(playerProfile);
        messagingTemplate.convertAndSendToUser(username, QUEUE_MATCH + "-status", "Waiting for an opponent...");

        tryToMatch();
    }

    private void tryToMatch() {
        synchronized (waitingPlayers) {
            if (waitingPlayers.size() >= 2) {
                var profileOne = waitingPlayers.poll();
                var profileTwo = waitingPlayers.poll();

                if (profileOne == null || profileTwo == null) {
                    return;
                }

                var playerOne = new Player();
                playerOne.setFirstTurn(true);

                var playerTwo = new Player();
                playerTwo.setFirstTurn(false);

                var match = Match.builder()
                        .playerOne(playerOne)
                        .playerTwo(playerTwo)
                        .currentPlayer(playerOne)
                        .turnNumber(1)
                        .battleState(BattleState.IN_BATTLE)
                        .build();

                activeMatches.put(profileOne.username(), match);
                activeMatches.put(profileTwo.username(), match);

                messagingTemplate.convertAndSendToUser(profileOne.username(), QUEUE_MATCH, profileTwo);
                messagingTemplate.convertAndSendToUser(profileTwo.username(), QUEUE_MATCH, profileOne);
            }
        }
    }

    public Match getMatch(String username) {
        return activeMatches.get(username);
    }

    public void endTurn(String username) {
        Match match = activeMatches.get(username);
        if (match != null) {
            match.endTurn();
            messagingTemplate.convertAndSendToUser(username, QUEUE_MATCH, match);
        }
    }
}
