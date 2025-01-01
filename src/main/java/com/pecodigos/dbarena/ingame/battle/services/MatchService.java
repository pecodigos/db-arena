package com.pecodigos.dbarena.ingame.battle.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pecodigos.dbarena.ingame.battle.dto.MatchInfoDTO;
import com.pecodigos.dbarena.ingame.battle.dto.TurnAction;
import com.pecodigos.dbarena.ingame.battle.dto.TurnActions;
import com.pecodigos.dbarena.ingame.battle.dto.mapper.MatchLogMapper;
import com.pecodigos.dbarena.ingame.battle.models.Fighter;
import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.ingame.battle.models.Player;
import com.pecodigos.dbarena.ingame.entities.MatchLog;
import com.pecodigos.dbarena.ingame.enums.battle.BattleQueueType;
import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
import com.pecodigos.dbarena.ingame.repositories.MatchLogRepository;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;
    private final MatchLogRepository matchLogRepository;
    private final MatchLogMapper matchLogMapper;

    private final Queue<Player> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final Map<String, Match> activeMatches = new ConcurrentHashMap<>();
    private final Map<String, BattleQueueType> playerQueueTypes = new ConcurrentHashMap<>();

    public void searchForMatch(String username, Fighter[] team, BattleQueueType queueType) {
        if (username == null || team == null || queueType == null) {
            throw new IllegalArgumentException("Invalid request.");
        }

        PublicProfileDTO playerProfile = userService.getPublicProfile(username);

        var player = new Player(playerProfile);

        player.setTeam(team);

        waitingPlayers.add(player);
        playerQueueTypes.put(username, queueType);
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

            playerQueueTypes.remove(playerOneUsername);
            playerQueueTypes.remove(playerTwoUsername);

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

    public void endTurn(String username, TurnActions turnActions) {
        Match match = activeMatches.get(username);
        if (match == null || !match.getCurrentPlayer().getUsername().equals(username)) {
            throw new IllegalStateException("Not your turn, or match not found.");
        }

        var currentPlayer = match.getCurrentPlayer();
        var opponentPlayer = match.getPlayerOne().getUsername().equals(username)
                ? match.getPlayerTwo()
                : match.getPlayerOne();

        try {
            executeActions(turnActions, currentPlayer, opponentPlayer);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Invalid actions.");
        }

        currentPlayer.reduceCooldowns();
        currentPlayer.generateEnergy();


        if (isMatchEnded(opponentPlayer)) {
            match.setBattleState(BattleState.FINISHED);
            endMatch(match, currentPlayer.getUsername());
            return;
        }

        match.setCurrentPlayer(opponentPlayer);
        match.setTurnNumber(match.getTurnNumber() + 1);

        updateMatchState(match);
    }

    private void executeActions(TurnActions turnActions, Player currentPlayer, Player opponentPlayer) {
        for (TurnAction action : turnActions.actions()) {
            var attacker = currentPlayer.getTeam()[action.characterIndex()];
            if (!attacker.isAlive() || attacker.isStunned()) {
                continue;
            }

            var selectedSkill = attacker.getSkills()[action.skillIndex()];
            Fighter[] targets = new Fighter[action.targetIndexes().size()];

            for (int i = 0; i < action.targetIndexes().size(); i++) {
                int targetIndex = action.targetIndexes().get(i);

                if (Boolean.TRUE.equals(selectedSkill.getAbility().getIsHarmful())) {
                    targets[i] = opponentPlayer.getTeam()[targetIndex];
                } else {
                    targets[i] = currentPlayer.getTeam()[targetIndex];
                }
            }

            try {
                if (Boolean.TRUE.equals(selectedSkill.getAbility().getIsHarmful())) {
                    currentPlayer.useHarmfulSkill(attacker, targets, selectedSkill);
                } else {
                    currentPlayer.useHelpfulSkill(targets, selectedSkill);
                }
            } catch (IllegalStateException e) {
                continue;
            }
        }
    }

    private void endMatch(Match match, String winnerUsername) {
        var matchLog = MatchLog.builder()
                        .playerOneUsername(match.getPlayerOne().getUsername())
                        .playerTwoUsername(match.getPlayerTwo().getUsername())
                        .winnerUsername(winnerUsername)
                        .battleQueueType(match.getBattleQueueType())
                        .build();

        var matchLogDTO = matchLogMapper.toDTO(matchLogRepository.save(matchLog));

        String playerOneUsername = match.getPlayerOne().getUsername();
        String playerTwoUsername = match.getPlayerTwo().getUsername();

        activeMatches.remove(playerOneUsername);
        activeMatches.remove(playerTwoUsername);
        redisTemplate.delete(REDIS_KEY + playerOneUsername);
        redisTemplate.delete(REDIS_KEY + playerTwoUsername);

        messagingTemplate.convertAndSendToUser(playerOneUsername, QUEUE_MATCH + "-end",
                matchLogDTO);
        messagingTemplate.convertAndSendToUser(playerTwoUsername, QUEUE_MATCH + "-end",
                matchLogDTO);
    }

    public String determineOpponentUsername(Match match, String username) {
        if (match.getPlayerOne().getUsername().equals(username)) {
            return match.getPlayerTwo().getUsername();
        }
        return match.getPlayerOne().getUsername();
    }

    public boolean isMatchEnded(Player opponentPlayer) {
        return Arrays.stream(opponentPlayer.getTeam()).noneMatch(Fighter::isAlive);
    }

    private void updateMatchState(Match match) {
        String playerOneUsername = match.getPlayerOne().getUsername();
        String playerTwoUsername = match.getPlayerTwo().getUsername();

        activeMatches.put(playerOneUsername, match);
        activeMatches.put(playerTwoUsername, match);

        redisTemplate.opsForValue().set(REDIS_KEY + playerOneUsername, match);
        redisTemplate.opsForValue().set(REDIS_KEY + playerTwoUsername, match);

        var playerOneOpponentProfile = userService.getPublicProfile(playerTwoUsername);
        var playerTwoOpponentProfile = userService.getPublicProfile(playerOneUsername);

        messagingTemplate.convertAndSendToUser(
                playerOneUsername,
                QUEUE_MATCH,
                new MatchInfoDTO(match, playerOneOpponentProfile)
        );

        messagingTemplate.convertAndSendToUser(
                playerTwoUsername,
                QUEUE_MATCH,
                new MatchInfoDTO(match, playerTwoOpponentProfile)
        );
    }

    public void notifyError(String username, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
                username,
                QUEUE_MATCH + "-error",
                errorMessage
        );
    }
}
