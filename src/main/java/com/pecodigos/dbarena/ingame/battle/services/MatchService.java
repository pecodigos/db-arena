package com.pecodigos.dbarena.ingame.battle.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pecodigos.dbarena.ingame.battle.dto.MatchInfoDTO;
import com.pecodigos.dbarena.ingame.battle.models.Fighter;
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

    private final Queue<Player> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final Map<String, Match> activeMatches = new ConcurrentHashMap<>();

    public void searchForMatch(String username, Fighter[] team) {
        PublicProfileDTO playerProfile = userService.getPublicProfile(username);

        var player = new Player(playerProfile);

        player.setTeam(team);

        waitingPlayers.add(player);
        messagingTemplate.convertAndSendToUser(username, QUEUE_MATCH + "-status", "Waiting for an opponent...");

        tryToMatch();
    }

    private void tryToMatch() {
        if (waitingPlayers.size() >= 2) {
            var playerOne = waitingPlayers.poll();
            var playerTwo = waitingPlayers.poll();

            if (playerOne == null || playerTwo == null) {
                return;
            }

            var playerOneUsername = playerOne.getUsername();
            var playerTwoUsername = playerTwo.getUsername();

            if (playerOneUsername.equals(playerTwoUsername)) {
                waitingPlayers.remove(playerOne);
                waitingPlayers.remove(playerTwo);
                return;
            }

            playerOne.setFirstTurn(true);
            playerTwo.setFirstTurn(false);

            var match = Match.builder()
                    .playerOne(playerOne)
                    .playerTwo(playerTwo)
                    .currentPlayer(playerOne)
                    .turnNumber(1)
                    .battleState(BattleState.IN_BATTLE)
                    .build();

            activeMatches.put(playerOneUsername, match);
            activeMatches.put(playerTwoUsername, match);

            redisTemplate.opsForValue().set(REDIS_KEY + playerOneUsername, match);
            redisTemplate.opsForValue().set(REDIS_KEY + playerTwoUsername, match);

            var matchInfoOne = new MatchInfoDTO(match, playerTwo.getUserProfile());
            var matchInfoTwo = new MatchInfoDTO(match, playerOne.getUserProfile());

            messagingTemplate.convertAndSendToUser(playerOneUsername, QUEUE_MATCH, matchInfoOne);
            messagingTemplate.convertAndSendToUser(playerTwoUsername, QUEUE_MATCH, matchInfoTwo);
        }
    }

    public MatchInfoDTO getMatch(String username) {
        ObjectMapper mapper = new ObjectMapper();
        var match = mapper.convertValue(redisTemplate.opsForValue().get(REDIS_KEY + username), Match.class);
        if (match != null) {
            String opponentUsername = determineOpponentUsername(match, username);
            var opponentProfile = userService.getPublicProfile(opponentUsername);
            return new MatchInfoDTO(match, opponentProfile);
        }
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
        if (match.getPlayerOne().getUsername().equals(username)) {
            return match.getPlayerTwo().getUsername();
        }
        return match.getPlayerOne().getUsername();
    }
}
