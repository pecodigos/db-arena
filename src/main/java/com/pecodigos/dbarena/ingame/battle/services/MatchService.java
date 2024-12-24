package com.pecodigos.dbarena.ingame.battle.services;

import com.pecodigos.dbarena.ingame.battle.dto.MatchInfoDTO;
import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.ingame.battle.models.Player;
import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class MatchService {

    private static final String QUEUE_MATCH = "/queue/match";
    private static final String REDIS_KEY = "match:";

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Queue<PublicProfileDTO> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final Map<String, Match> activeMatches = new ConcurrentHashMap<>();

    public void searchForMatch(String username) {
        PublicProfileDTO playerProfile = userService.getPublicProfile(username);

        System.out.println("Player searching for match: " + username);

        waitingPlayers.add(playerProfile);
        messagingTemplate.convertAndSendToUser(username, QUEUE_MATCH + "-status", "Waiting for an opponent...");

        System.out.println("Player added to queue: " + username);

        tryToMatch();
    }

    private void tryToMatch() {
        if (waitingPlayers.size() >= 2) {
            var profileOne = waitingPlayers.poll();
            var profileTwo = waitingPlayers.poll();

            if (profileOne == null || profileTwo == null) {
                return;
            }

            var playerOne = new Player(profileOne);
            playerOne.setFirstTurn(true);

            var playerTwo = new Player(profileTwo);
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

            redisTemplate.opsForValue().set(REDIS_KEY + profileOne.username(), match);
            redisTemplate.opsForValue().set(REDIS_KEY + profileTwo.username(), match);

            System.out.println("Player One Profile info: " + playerOne.getUserProfile().toString());
            System.out.println("Player Two Profile info: " + playerTwo.getUserProfile().toString());
            var matchInfoOne = new MatchInfoDTO(match, playerTwo.getUserProfile());
            var matchInfoTwo = new MatchInfoDTO(match, playerOne.getUserProfile());

            messagingTemplate.convertAndSendToUser(profileOne.username(), QUEUE_MATCH, matchInfoOne);
            messagingTemplate.convertAndSendToUser(profileTwo.username(), QUEUE_MATCH, matchInfoTwo);
        }
    }

    public MatchInfoDTO getMatch(String username) {
        var match = (Match) redisTemplate.opsForValue().get(REDIS_KEY + username);
        if (match != null) {
            System.out.println("Match info: " + match);
            String opponentUsername = determineOpponentUsername(match, username);
            System.out.println("Retrieving opponent profile for username: " + opponentUsername);
            var opponentProfile = userService.getPublicProfile(opponentUsername);
            return new MatchInfoDTO(match, opponentProfile);
        }
        System.out.println("Match info returns null");
        return null;
    }

    public void endTurn(String username) {
        Match match = activeMatches.get(username);
        if (match != null) {
            match.endTurn();
            String opponentUsername = determineOpponentUsername(match, username);
            var opponentProfile = userService.getPublicProfile(opponentUsername);
            messagingTemplate.convertAndSendToUser(username, QUEUE_MATCH, new MatchInfoDTO(match, opponentProfile));
        }
    }

    public String determineOpponentUsername(Match match, String username) {
        System.out.println("Player one username: " + match.getPlayerOne().getUsername());
        if (match.getPlayerOne().getUsername().equals(username)) {
            System.out.println("Player two username: " + match.getPlayerTwo().getUsername());
            return match.getPlayerTwo().getUsername();
        }

        return match.getPlayerOne().getUsername();
    }
}
