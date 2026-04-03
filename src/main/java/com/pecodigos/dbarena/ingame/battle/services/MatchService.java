package com.pecodigos.dbarena.ingame.battle.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pecodigos.dbarena.ingame.battle.dto.MatchInfoDTO;
import com.pecodigos.dbarena.ingame.battle.dto.MatchLogDTO;
import com.pecodigos.dbarena.ingame.battle.dto.TurnAction;
import com.pecodigos.dbarena.ingame.battle.dto.TurnActions;
import com.pecodigos.dbarena.ingame.battle.dto.mapper.MatchLogMapper;
import com.pecodigos.dbarena.ingame.battle.models.Fighter;
import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.ingame.battle.models.Player;
import com.pecodigos.dbarena.ingame.battle.models.Skill;
import com.pecodigos.dbarena.ingame.entities.Ability;
import com.pecodigos.dbarena.ingame.entities.AbilityCost;
import com.pecodigos.dbarena.ingame.entities.Character;
import com.pecodigos.dbarena.ingame.entities.MatchLog;
import com.pecodigos.dbarena.ingame.enums.battle.BattleQueueType;
import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import com.pecodigos.dbarena.ingame.enums.skills.Distance;
import com.pecodigos.dbarena.ingame.repositories.AbilityRepository;
import com.pecodigos.dbarena.ingame.repositories.CharacterRepository;
import com.pecodigos.dbarena.ingame.repositories.MatchLogRepository;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.hibernate.LazyInitializationException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class MatchService {

    private static final String QUEUE_MATCH = "/queue/match";
    private static final String REDIS_KEY = "match:";
    private static final int PLAYER_TEAM_SIZE = 3;
    private static final int FIRST_TURN_INITIAL_ENERGY = 1;
    private static final int SECOND_TURN_INITIAL_ENERGY = 3;
    private static final String EARLY_KRILLIN_NAME = "Early Krillin";
    private static final String EARLY_PICCOLO_NAME = "Early Piccolo";
    private static final String EARLY_VEGETA_NAME = "Early Vegeta";
    private static final String DESTRUCTO_DISC_NAME = "Destructo Disc";
    private static final String PROTECT_ALLY_NAME = "Protect an Ally";
    private static final String GALICK_GUN_NAME = "Galick Gun";
    private static final String EXPLOSION_WAVE_NAME = "Explosion Wave";
    private static final String INVALID_ACTIONS_MESSAGE = "Invalid actions.";

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;
    private final CharacterRepository characterRepository;
    private final AbilityRepository abilityRepository;
    private final MatchLogRepository matchLogRepository;
    private final MatchLogMapper matchLogMapper;
    private final ObjectMapper objectMapper;

    private final Queue<Player> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final Map<String, Match> activeMatches = new ConcurrentHashMap<>();
    private final Map<String, BattleQueueType> playerQueueTypes = new ConcurrentHashMap<>();

    public void searchForMatch(String username, List<Long> characterIds, BattleQueueType queueType) {
        if (username == null || username.isBlank() || characterIds == null || queueType == null) {
            throw new IllegalArgumentException("Invalid request.");
        }

        if (characterIds.size() != PLAYER_TEAM_SIZE) {
            throw new IllegalArgumentException("You must select exactly 3 characters.");
        }

        if (new HashSet<>(characterIds).size() != characterIds.size()) {
            throw new IllegalArgumentException("Duplicate characters are not allowed.");
        }

        if (hasActiveMatch(username)) {
            throw new IllegalStateException("You are already in a match.");
        }

        if (waitingPlayers.stream().anyMatch(player -> username.equals(player.getUsername()))) {
            throw new IllegalStateException("You are already in queue.");
        }

        Fighter[] team = buildTeam(characterIds);

        PublicProfileDTO playerProfile = userService.getPublicProfile(username);

        var player = new Player(playerProfile);

        player.setTeam(team);

        waitingPlayers.add(player);
        playerQueueTypes.put(username, queueType);
        messagingTemplate.convertAndSendToUser(username, QUEUE_MATCH + "-status", "Waiting for an opponent...");

        tryToMatch();
    }

    private Fighter[] buildTeam(List<Long> characterIds) {
        List<Character> characters = characterRepository.findAllByIdIn(characterIds);
        if (characters.size() != characterIds.size()) {
            throw new IllegalArgumentException("One or more selected characters are invalid.");
        }

        Map<Long, Ability> abilitiesWithCostById = loadAbilitiesWithCost(characters);

        Map<Long, Character> charactersById = new HashMap<>();
        for (Character character : characters) {
            charactersById.put(character.getId(), character);
        }

        Fighter[] team = new Fighter[characterIds.size()];
        for (int i = 0; i < characterIds.size(); i++) {
            Character character = charactersById.get(characterIds.get(i));
            if (character == null) {
                throw new IllegalArgumentException("One or more selected characters are invalid.");
            }
            team[i] = createFighter(character, abilitiesWithCostById);
        }

        return team;
    }

    private Map<Long, Ability> loadAbilitiesWithCost(List<Character> characters) {
        List<Long> abilityIds = new ArrayList<>();
        for (Character character : characters) {
            for (Ability ability : Optional.ofNullable(character.getAbilities()).orElse(Collections.emptyList())) {
                if (ability.getId() != null) {
                    abilityIds.add(ability.getId());
                }
            }
        }

        if (abilityIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Ability> abilityById = new HashMap<>();
        for (Ability ability : abilityRepository.findAllByIdIn(abilityIds)) {
            abilityById.put(ability.getId(), ability);
        }

        return abilityById;
    }

    private Fighter createFighter(Character character, Map<Long, Ability> abilitiesWithCostById) {
        Fighter fighter = new Fighter();
        hydrateCharacterAbilities(character, abilitiesWithCostById);
        fighter.setCharacter(character);
        fighter.setSkills(createSkills(character, abilitiesWithCostById));
        fighter.setCurrentHp(100);
        fighter.setCurrentDestructibleDefense(0);
        fighter.setCurrentDamageReduction(0);
        fighter.setDamageReductionTurns(0);
        fighter.setCurrentBonusDamage(0);
        fighter.setBonusDamageTurns(0);
        fighter.setWeaknessAmount(0);
        fighter.setWeaknessTurns(0);
        fighter.setStunned(false);
        fighter.setStunTurns(0);
        fighter.setUnableToBecomeInvulnerable(false);
        fighter.setInvulnerable(false);
        fighter.setInvulnerableTurns(0);
        fighter.setAlive(true);
        fighter.setActiveEffects(new ArrayList<>());
        return fighter;
    }

    private void hydrateCharacterAbilities(Character character, Map<Long, Ability> abilitiesWithCostById) {
        List<Ability> abilities = Optional.ofNullable(character.getAbilities())
                .orElse(Collections.emptyList());

        if (abilities.isEmpty()) {
            character.setAbilities(new ArrayList<>());
            return;
        }

        List<Ability> hydratedAbilities = new ArrayList<>(abilities.size());
        for (Ability ability : abilities) {
            Ability hydratedAbility = abilitiesWithCostById.getOrDefault(ability.getId(), ability);
            hydratedAbility.setCost(materializeAbilityCost(hydratedAbility));
            hydratedAbilities.add(hydratedAbility);
        }

        character.setAbilities(hydratedAbilities);
    }

    private Skill[] createSkills(Character character, Map<Long, Ability> abilitiesWithCostById) {
        List<Ability> abilities = Optional.ofNullable(character.getAbilities()).orElse(Collections.emptyList());
        Skill[] skills = new Skill[abilities.size()];
        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = abilities.get(i);
            Ability abilityWithCost = abilitiesWithCostById.getOrDefault(ability.getId(), ability);
            skills[i] = createSkill(abilityWithCost);
        }
        return skills;
    }

    private Skill createSkill(Ability ability) {
        Skill skill = new Skill();
        skill.setAbility(ability);
        skill.setCurrentCooldown(0);
        return skill;
    }

    private void tryToMatch() {
        while (waitingPlayers.size() >= 2) {
            Player playerOne = findFirstMatchablePlayer();
            if (playerOne == null) {
                return;
            }

            BattleQueueType queueType = playerQueueTypes.get(playerOne.getUsername());
            Player playerTwo = findOpponentForQueue(playerOne.getUsername(), queueType);
            if (playerTwo == null) {
                return;
            }

            if (!waitingPlayers.remove(playerOne) || !waitingPlayers.remove(playerTwo)) {
                continue;
            }

            createMatch(playerOne, playerTwo, queueType);
        }
    }

    private Player findFirstMatchablePlayer() {
        for (Player candidate : waitingPlayers) {
            BattleQueueType queueType = playerQueueTypes.get(candidate.getUsername());
            if (queueType == null) {
                waitingPlayers.remove(candidate);
                continue;
            }

            if (findOpponentForQueue(candidate.getUsername(), queueType) != null) {
                return candidate;
            }
        }

        return null;
    }

    private Player findOpponentForQueue(String username, BattleQueueType queueType) {
        for (Player candidate : waitingPlayers) {
            String candidateUsername = candidate.getUsername();
            if (candidateUsername.equals(username)) {
                continue;
            }

            BattleQueueType candidateQueueType = playerQueueTypes.get(candidateUsername);
            if (queueType == candidateQueueType) {
                return candidate;
            }
        }

        return null;
    }

    private void createMatch(Player playerOne, Player playerTwo, BattleQueueType queueType) {
        String playerOneUsername = playerOne.getUsername();
        String playerTwoUsername = playerTwo.getUsername();

        if (playerOneUsername.equals(playerTwoUsername)) {
            return;
        }

        playerOne.setFirstTurn(true);
        playerTwo.setFirstTurn(false);
        playerOne.grantInitialEnergy(FIRST_TURN_INITIAL_ENERGY);
        playerTwo.grantInitialEnergy(SECOND_TURN_INITIAL_ENERGY);

        BattleQueueType battleQueueType = Optional.ofNullable(queueType)
                .orElse(BattleQueueType.QUICK);

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(1)
                .battleState(BattleState.IN_BATTLE)
                .battleQueueType(battleQueueType)
                .build();
        ensureCurrentTurnPrepared(match);

        activeMatches.put(playerOneUsername, match);
        activeMatches.put(playerTwoUsername, match);

        playerQueueTypes.remove(playerOneUsername);
        playerQueueTypes.remove(playerTwoUsername);

        persistMatch(match);

        MatchInfoDTO matchInfoOne = new MatchInfoDTO(match, playerTwo.getUserProfile());
        MatchInfoDTO matchInfoTwo = new MatchInfoDTO(match, playerOne.getUserProfile());

        messagingTemplate.convertAndSendToUser(playerOneUsername, QUEUE_MATCH, matchInfoOne);
        messagingTemplate.convertAndSendToUser(playerTwoUsername, QUEUE_MATCH, matchInfoTwo);
    }

    public MatchInfoDTO getMatch(String username) {
        Match match = getOrLoadMatch(username);
        if (match != null) {
            String opponentUsername = determineOpponentUsername(match, username);
            var opponentProfile = userService.getPublicProfile(opponentUsername);
            return new MatchInfoDTO(match, opponentProfile);
        }
        return null;
    }

    public boolean cancelSearch(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        boolean cancelled = waitingPlayers.removeIf(player -> username.equals(player.getUsername()));
        playerQueueTypes.remove(username);
        return cancelled;
    }

    public void endTurn(String username, TurnActions turnActions) {
        Match match = getOrLoadMatch(username);
        if (match == null || match.getCurrentPlayer() == null || !match.getCurrentPlayer().getUsername().equals(username)) {
            throw new IllegalStateException("Not your turn, or match not found.");
        }

        normalizeLegacyMatchData(match);
        ensureCurrentTurnPrepared(match);

        var currentPlayer = match.getCurrentPlayer();
        var opponentPlayer = match.getPlayerOne().getUsername().equals(username)
                ? match.getPlayerTwo()
                : match.getPlayerOne();

        validateTurnActions(turnActions, currentPlayer, opponentPlayer);
        executeActions(turnActions, currentPlayer, opponentPlayer);

        currentPlayer.reduceCooldowns();
        currentPlayer.generateEnergy();


        if (isMatchEnded(opponentPlayer)) {
            match.setBattleState(BattleState.FINISHED);
            endMatch(match, currentPlayer.getUsername());
            return;
        }

        match.setCurrentPlayer(opponentPlayer);
        match.setTurnNumber(match.getTurnNumber() + 1);
        match.setCurrentTurnPrepared(false);

        updateMatchState(match);
    }

    private void validateTurnActions(TurnActions turnActions, Player currentPlayer, Player opponentPlayer) {
        if (turnActions == null || turnActions.actions() == null) {
            return;
        }

        Player simulatedCurrentPlayer = deepCopyPlayer(currentPlayer);
        Player simulatedOpponentPlayer = deepCopyPlayer(opponentPlayer);
        executeActions(turnActions, simulatedCurrentPlayer, simulatedOpponentPlayer);
    }

    private void executeActions(TurnActions turnActions, Player currentPlayer, Player opponentPlayer) {
        if (turnActions == null || turnActions.actions() == null) {
            return;
        }

        Set<Integer> queuedCharacters = new HashSet<>();
        for (TurnAction action : turnActions.actions()) {
            ResolvedTurnAction resolvedTurnAction = resolveTurnAction(action, currentPlayer, opponentPlayer, queuedCharacters);

            if (resolvedTurnAction.harmfulSkill()) {
                currentPlayer.useHarmfulSkill(
                        resolvedTurnAction.attacker(),
                        resolvedTurnAction.targets(),
                        resolvedTurnAction.selectedSkill(),
                        resolvedTurnAction.anyEnergyChoices(),
                        opponentPlayer
                );
            } else {
                currentPlayer.useHelpfulSkill(
                        resolvedTurnAction.targets(),
                        resolvedTurnAction.selectedSkill(),
                        resolvedTurnAction.anyEnergyChoices()
                );
            }
        }
    }

    private void endMatch(Match match, String winnerUsername) {
        String playerOneUsername = match.getPlayerOne().getUsername();
        String playerTwoUsername = match.getPlayerTwo().getUsername();

        userService.recordMatchResult(playerOneUsername, playerTwoUsername, winnerUsername);

        var matchLog = MatchLog.builder()
                        .playerOneUsername(playerOneUsername)
                        .playerTwoUsername(playerTwoUsername)
                        .winnerUsername(winnerUsername)
                        .battleQueueType(match.getBattleQueueType())
                        .build();

        MatchLog savedMatchLog = matchLogRepository.save(matchLog);
        MatchLogDTO mappedMatchLogDTO = matchLogMapper.toDTO(savedMatchLog);
        MatchLogDTO matchLogDTO = hydrateMatchLogProfiles(mappedMatchLogDTO, savedMatchLog);

        activeMatches.remove(playerOneUsername);
        activeMatches.remove(playerTwoUsername);
        redisTemplate.delete(REDIS_KEY + playerOneUsername);
        redisTemplate.delete(REDIS_KEY + playerTwoUsername);

        messagingTemplate.convertAndSendToUser(playerOneUsername, QUEUE_MATCH + "-end",
                matchLogDTO);
        messagingTemplate.convertAndSendToUser(playerTwoUsername, QUEUE_MATCH + "-end",
                matchLogDTO);
    }

    private MatchLogDTO hydrateMatchLogProfiles(MatchLogDTO mappedMatchLogDTO, MatchLog savedMatchLog) {
        PublicProfileDTO playerOneProfile = mappedMatchLogDTO != null ? mappedMatchLogDTO.playerOne() : null;
        PublicProfileDTO playerTwoProfile = mappedMatchLogDTO != null ? mappedMatchLogDTO.playerTwo() : null;
        PublicProfileDTO winnerProfile = mappedMatchLogDTO != null ? mappedMatchLogDTO.winner() : null;

        if (playerOneProfile == null && savedMatchLog.getPlayerOneUsername() != null && !savedMatchLog.getPlayerOneUsername().isBlank()) {
            playerOneProfile = userService.getPublicProfile(savedMatchLog.getPlayerOneUsername());
        }

        if (playerTwoProfile == null && savedMatchLog.getPlayerTwoUsername() != null && !savedMatchLog.getPlayerTwoUsername().isBlank()) {
            playerTwoProfile = userService.getPublicProfile(savedMatchLog.getPlayerTwoUsername());
        }

        if (winnerProfile == null && savedMatchLog.getWinnerUsername() != null && !savedMatchLog.getWinnerUsername().isBlank()) {
            winnerProfile = userService.getPublicProfile(savedMatchLog.getWinnerUsername());
        }

        Long id = mappedMatchLogDTO != null ? mappedMatchLogDTO.id() : savedMatchLog.getId();
        BattleQueueType battleQueueType = mappedMatchLogDTO != null ? mappedMatchLogDTO.battleQueueType() : savedMatchLog.getBattleQueueType();
        var battleDate = mappedMatchLogDTO != null ? mappedMatchLogDTO.battleDate() : savedMatchLog.getBattleDate();

        return new MatchLogDTO(
                id,
                playerOneProfile,
                playerTwoProfile,
                winnerProfile,
                battleQueueType,
                battleDate
        );
    }

    public String determineOpponentUsername(Match match, String username) {
        if (!isMatchParticipant(match, username)) {
            throw new IllegalStateException("Match not found.");
        }

        if (match.getPlayerOne().getUsername().equals(username)) {
            return match.getPlayerTwo().getUsername();
        }
        return match.getPlayerOne().getUsername();
    }

    public boolean isMatchEnded(Player opponentPlayer) {
        return Arrays.stream(opponentPlayer.getTeam()).noneMatch(Fighter::isAlive);
    }

    public void forfeitMatch(String username) {
        Match match = getOrLoadMatch(username);
        if (match == null) {
            throw new IllegalStateException("Match not found.");
        }

        match.setBattleState(BattleState.FINISHED);
        String winnerUsername = determineOpponentUsername(match, username);
        endMatch(match, winnerUsername);
    }

    private void updateMatchState(Match match) {
        ensureCurrentTurnPrepared(match);

        String playerOneUsername = match.getPlayerOne().getUsername();
        String playerTwoUsername = match.getPlayerTwo().getUsername();

        activeMatches.put(playerOneUsername, match);
        activeMatches.put(playerTwoUsername, match);

        persistMatch(match);

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

    private boolean hasActiveMatch(String username) {
        Match match = getOrLoadMatch(username);
        if (match == null) {
            return false;
        }

        return match.getBattleState() != BattleState.FINISHED;
    }

    private Match getOrLoadMatch(String username) {
        Match match = activeMatches.get(username);
        if (match != null) {
            if (!isMatchParticipant(match, username)) {
                activeMatches.remove(username);
            } else {
                normalizeLegacyMatchData(match);
                if (ensureCurrentTurnPrepared(match)) {
                    persistMatch(match);
                }
                return match;
            }
        }

        if (username == null || username.isBlank()) {
            return match;
        }

        Object cachedMatch = redisTemplate.opsForValue().get(REDIS_KEY + username);
        Match hydratedMatch = toMatch(cachedMatch);
        if (hydratedMatch == null) {
            return null;
        }

        if (!isMatchParticipant(hydratedMatch, username)) {
            activeMatches.remove(username);
            redisTemplate.delete(REDIS_KEY + username);
            return null;
        }

        normalizeLegacyMatchData(hydratedMatch);
        ensureCurrentTurnPrepared(hydratedMatch);
        cacheMatch(hydratedMatch);
        persistMatch(hydratedMatch);

        return hydratedMatch;
    }

    private Match toMatch(Object cachedMatch) {
        if (cachedMatch == null) {
            return null;
        }

        if (cachedMatch instanceof Match match) {
            return match;
        }

        try {
            return objectMapper.convertValue(cachedMatch, Match.class);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void cacheMatch(Match match) {
        if (match == null || match.getPlayerOne() == null || match.getPlayerTwo() == null) {
            return;
        }

        activeMatches.put(match.getPlayerOne().getUsername(), match);
        activeMatches.put(match.getPlayerTwo().getUsername(), match);
    }

    private void persistMatch(Match match) {
        if (match == null || match.getPlayerOne() == null || match.getPlayerTwo() == null) {
            return;
        }

        redisTemplate.opsForValue().set(REDIS_KEY + match.getPlayerOne().getUsername(), match);
        redisTemplate.opsForValue().set(REDIS_KEY + match.getPlayerTwo().getUsername(), match);
    }

    private void normalizeLegacyMatchData(Match match) {
        if (match == null) {
            return;
        }

        relinkCurrentPlayer(match);
        normalizeLegacyPlayerAbilities(match.getPlayerOne());
        normalizeLegacyPlayerAbilities(match.getPlayerTwo());
    }

    private void relinkCurrentPlayer(Match match) {
        if (match == null || match.getCurrentPlayer() == null) {
            return;
        }

        String currentUsername = match.getCurrentPlayer().getUsername();
        if (currentUsername == null || currentUsername.isBlank()) {
            return;
        }

        if (match.getPlayerOne() != null && currentUsername.equals(match.getPlayerOne().getUsername())) {
            match.setCurrentPlayer(match.getPlayerOne());
            return;
        }

        if (match.getPlayerTwo() != null && currentUsername.equals(match.getPlayerTwo().getUsername())) {
            match.setCurrentPlayer(match.getPlayerTwo());
        }
    }

    private boolean ensureCurrentTurnPrepared(Match match) {
        if (match == null || match.getCurrentPlayer() == null || match.isCurrentTurnPrepared()) {
            return false;
        }

        match.getCurrentPlayer().prepareForTurn();
        match.setCurrentTurnPrepared(true);
        return true;
    }

    private void normalizeLegacyPlayerAbilities(Player player) {
        if (player == null || player.getTeam() == null) {
            return;
        }

        for (Fighter fighter : player.getTeam()) {
            sanitizeFighterCharacterAbilities(fighter);

            if (fighter == null || fighter.getSkills() == null) {
                continue;
            }

            for (Skill skill : fighter.getSkills()) {
                normalizeLegacyAbilityFlags(fighter, skill);
            }
        }
    }

    private void sanitizeFighterCharacterAbilities(Fighter fighter) {
        if (fighter == null || fighter.getCharacter() == null) {
            return;
        }

        Character character = fighter.getCharacter();
        List<Ability> characterAbilities = Optional.ofNullable(character.getAbilities())
                .orElse(Collections.emptyList());

        if (characterAbilities.isEmpty()) {
            character.setAbilities(new ArrayList<>());
            return;
        }

        Map<Long, Ability> skillsAbilityById = new HashMap<>();
        for (Skill skill : Optional.ofNullable(fighter.getSkills()).orElse(new Skill[0])) {
            if (skill == null || skill.getAbility() == null || skill.getAbility().getId() == null) {
                continue;
            }

            skillsAbilityById.put(skill.getAbility().getId(), skill.getAbility());
        }

        List<Ability> hydratedAbilities = new ArrayList<>(characterAbilities.size());
        for (Ability characterAbility : characterAbilities) {
            Ability hydratedAbility = skillsAbilityById.getOrDefault(characterAbility.getId(), characterAbility);
            hydratedAbility.setCost(materializeAbilityCost(hydratedAbility));
            hydratedAbilities.add(hydratedAbility);
        }

        character.setAbilities(hydratedAbilities);
    }

    private List<AbilityCost> materializeAbilityCost(Ability ability) {
        if (ability == null) {
            return new ArrayList<>();
        }

        try {
            return new ArrayList<>(Optional.ofNullable(ability.getCost()).orElse(Collections.emptyList()));
        } catch (LazyInitializationException ignored) {
            return new ArrayList<>();
        }
    }

    private void normalizeLegacyAbilityFlags(Fighter attacker, Skill selectedSkill) {
        if (attacker == null || selectedSkill == null || selectedSkill.getAbility() == null) {
            return;
        }

        Ability ability = selectedSkill.getAbility();
        String attackerName = Optional.ofNullable(attacker.getCharacter())
                .map(Character::getName)
                .orElse("");

        if (EARLY_KRILLIN_NAME.equals(attackerName)
                && DESTRUCTO_DISC_NAME.equals(ability.getName())
                && !Boolean.TRUE.equals(ability.getIsHarmful())) {
            ability.setIsHarmful(true);
        }

        if (EARLY_PICCOLO_NAME.equals(attackerName)
                && PROTECT_ALLY_NAME.equals(ability.getName())
                && !Objects.equals(ability.getDurationInTurns(), 1)) {
            ability.setDurationInTurns(1);
        }

        if (EARLY_VEGETA_NAME.equals(attackerName)
                && EXPLOSION_WAVE_NAME.equals(ability.getName())
                && !Boolean.TRUE.equals(ability.getIsHarmful())) {
            ability.setIsHarmful(true);
        }

        if (EARLY_VEGETA_NAME.equals(attackerName)
                && GALICK_GUN_NAME.equals(ability.getName())
                && !Objects.equals(ability.getSecondaryDamage(), 10)) {
            ability.setSecondaryDamage(10);
        }
    }

    private boolean isMatchParticipant(Match match, String username) {
        if (match == null || username == null || username.isBlank()) {
            return false;
        }

        String playerOneUsername = Optional.ofNullable(match.getPlayerOne())
                .map(Player::getUsername)
                .orElse(null);
        String playerTwoUsername = Optional.ofNullable(match.getPlayerTwo())
                .map(Player::getUsername)
                .orElse(null);

        return username.equals(playerOneUsername) || username.equals(playerTwoUsername);
    }

    private Player deepCopyPlayer(Player player) {
        try {
            return objectMapper.convertValue(player, Player.class);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(INVALID_ACTIONS_MESSAGE, e);
        }
    }

    private ResolvedTurnAction resolveTurnAction(
            TurnAction action,
            Player currentPlayer,
            Player opponentPlayer,
            Set<Integer> queuedCharacters
    ) {
        if (action == null || currentPlayer == null || currentPlayer.getTeam() == null || opponentPlayer == null) {
            throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
        }

        int characterIndex = action.characterIndex();
        if (characterIndex < 0 || characterIndex >= currentPlayer.getTeam().length || !queuedCharacters.add(characterIndex)) {
            throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
        }

        Fighter attacker = currentPlayer.getTeam()[characterIndex];
        if (attacker == null || !attacker.isAlive() || attacker.isStunned() || attacker.getSkills() == null) {
            throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
        }

        int skillIndex = action.skillIndex();
        if (skillIndex < 0 || skillIndex >= attacker.getSkills().length) {
            throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
        }

        Skill selectedSkill = attacker.getSkills()[skillIndex];
        if (selectedSkill == null || selectedSkill.getAbility() == null) {
            throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
        }

        normalizeLegacyAbilityFlags(attacker, selectedSkill);
        boolean harmfulSkill = Boolean.TRUE.equals(selectedSkill.getAbility().getIsHarmful());
        Fighter[] targets = resolveTargets(action, currentPlayer, opponentPlayer, attacker, selectedSkill, harmfulSkill);
        Map<EnergyType, Integer> anyEnergyChoices = sanitizeAnyEnergyChoices(action.anyEnergyChoices());

        return new ResolvedTurnAction(attacker, selectedSkill, targets, harmfulSkill, anyEnergyChoices);
    }

    private Fighter[] resolveTargets(
            TurnAction action,
            Player currentPlayer,
            Player opponentPlayer,
            Fighter attacker,
            Skill selectedSkill,
            boolean harmfulSkill
    ) {
        if (selectedSkill.getAbility().getDistance() == Distance.NONE) {
            return new Fighter[]{attacker};
        }

        Fighter[] targetPool = harmfulSkill ? opponentPlayer.getTeam() : currentPlayer.getTeam();
        List<Integer> targetIndexes = normalizeTargetIndexes(action, targetPool, selectedSkill, harmfulSkill);
        if (targetIndexes.isEmpty()) {
            throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
        }

        Set<Integer> seenTargets = new HashSet<>();
        Fighter[] targets = new Fighter[targetIndexes.size()];
        for (int i = 0; i < targetIndexes.size(); i++) {
            Integer targetIndex = targetIndexes.get(i);
            if (targetIndex == null || targetIndex < 0 || targetIndex >= targetPool.length || !seenTargets.add(targetIndex)) {
                throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
            }

            Fighter target = targetPool[targetIndex];
            if (target == null || !target.isAlive() || (harmfulSkill && target.isInvulnerable())) {
                throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
            }

            targets[i] = target;
        }

        return targets;
    }

    private List<Integer> normalizeTargetIndexes(
            TurnAction action,
            Fighter[] targetPool,
            Skill selectedSkill,
            boolean harmfulSkill
    ) {
        List<Integer> requestedTargetIndexes = Optional.ofNullable(action.targetIndexes()).orElse(Collections.emptyList());
        if (selectedSkill.getAbility().getEffectType() == com.pecodigos.dbarena.ingame.enums.skills.EffectType.AOE) {
            List<Integer> inferredTargetIndexes = new ArrayList<>();
            for (int i = 0; i < targetPool.length; i++) {
                Fighter target = targetPool[i];
                if (target == null || !target.isAlive()) {
                    continue;
                }
                if (harmfulSkill && target.isInvulnerable()) {
                    continue;
                }
                inferredTargetIndexes.add(i);
            }

            if (requestedTargetIndexes.isEmpty()) {
                return inferredTargetIndexes;
            }

            List<Integer> normalizedTargetIndexes = new ArrayList<>();
            for (Integer requestedTargetIndex : requestedTargetIndexes) {
                if (requestedTargetIndex != null && inferredTargetIndexes.contains(requestedTargetIndex)) {
                    normalizedTargetIndexes.add(requestedTargetIndex);
                }
            }

            for (Integer inferredTargetIndex : inferredTargetIndexes) {
                if (!normalizedTargetIndexes.contains(inferredTargetIndex)) {
                    normalizedTargetIndexes.add(inferredTargetIndex);
                }
            }

            return normalizedTargetIndexes;
        }

        if (requestedTargetIndexes.size() != 1) {
            throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
        }

        return requestedTargetIndexes;
    }

    private Map<EnergyType, Integer> sanitizeAnyEnergyChoices(Map<EnergyType, Integer> anyEnergyChoices) {
        if (anyEnergyChoices == null || anyEnergyChoices.isEmpty()) {
            return null;
        }

        Map<EnergyType, Integer> sanitizedChoices = new EnumMap<>(EnergyType.class);
        for (Map.Entry<EnergyType, Integer> entry : anyEnergyChoices.entrySet()) {
            EnergyType energyType = entry.getKey();
            Integer amount = entry.getValue();

            if (energyType == null
                    || amount == null
                    || amount <= 0
                    || energyType == EnergyType.ANY
                    || energyType == EnergyType.NONE) {
                throw new IllegalStateException(INVALID_ACTIONS_MESSAGE);
            }

            sanitizedChoices.put(energyType, amount);
        }

        return sanitizedChoices;
    }

    private record ResolvedTurnAction(
            Fighter attacker,
            Skill selectedSkill,
            Fighter[] targets,
            boolean harmfulSkill,
            Map<EnergyType, Integer> anyEnergyChoices
    ) {
    }
}
