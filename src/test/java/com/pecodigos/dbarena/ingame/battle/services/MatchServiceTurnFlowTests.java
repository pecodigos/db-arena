package com.pecodigos.dbarena.ingame.battle.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pecodigos.dbarena.ingame.battle.dto.TurnAction;
import com.pecodigos.dbarena.ingame.battle.dto.TurnActions;
import com.pecodigos.dbarena.ingame.battle.dto.MatchInfoDTO;
import com.pecodigos.dbarena.ingame.battle.dto.MatchLogDTO;
import com.pecodigos.dbarena.ingame.battle.dto.mapper.MatchLogMapper;
import com.pecodigos.dbarena.ingame.battle.models.ActiveEffect;
import com.pecodigos.dbarena.ingame.battle.models.Fighter;
import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.ingame.battle.models.Player;
import com.pecodigos.dbarena.ingame.battle.models.Skill;
import com.pecodigos.dbarena.ingame.entities.Ability;
import com.pecodigos.dbarena.ingame.entities.AbilityCost;
import com.pecodigos.dbarena.ingame.entities.Character;
import com.pecodigos.dbarena.ingame.entities.MatchLog;
import com.pecodigos.dbarena.ingame.enums.battle.BattleQueueType;
import com.pecodigos.dbarena.ingame.repositories.AbilityRepository;
import com.pecodigos.dbarena.ingame.repositories.CharacterRepository;
import com.pecodigos.dbarena.ingame.repositories.MatchLogRepository;
import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import com.pecodigos.dbarena.ingame.enums.skills.DamageType;
import com.pecodigos.dbarena.ingame.enums.skills.Distance;
import com.pecodigos.dbarena.ingame.enums.skills.EffectType;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.enums.Rank;
import com.pecodigos.dbarena.user.enums.Role;
import com.pecodigos.dbarena.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

class MatchServiceTurnFlowTests {

    @Test
    void endTurnShouldApplyActionsAndPassTurnToOpponent() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());

        Fighter attacker = newFighter(100);
        attacker.setSkills(new Skill[]{newSkill(20, List.of(cost(EnergyType.KI, 1)))});
        playerOne.setTeam(new Fighter[]{attacker, newFighter(100), newFighter(100)});
        playerOne.setEnergyPool(new EnumMap<>(EnergyType.class));
        playerOne.getEnergyPool().put(EnergyType.KI, 1);

        Fighter defender = newFighter(100);
        playerTwo.setTeam(new Fighter[]{defender, newFighter(100), newFighter(100)});
        playerTwo.setEnergyPool(new EnumMap<>(EnergyType.class));

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(1)
                .battleState(BattleState.IN_BATTLE)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");
        activeMatches.put(playerOne.getUsername(), match);
        activeMatches.put(playerTwo.getUsername(), match);

        matchService.endTurn(playerOne.getUsername(), new TurnActions(List.of(new TurnAction(0, 0, List.of(0), null))));

        assertEquals(80, defender.getCurrentHp());
        assertSame(playerTwo, match.getCurrentPlayer());
        assertEquals(2, match.getTurnNumber());
        assertEquals(3, playerOne.getEnergyPool().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void endTurnShouldAllowPassingWithoutActions() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());

        playerOne.setTeam(new Fighter[]{newFighter(100), newFighter(100), newFighter(100)});
        playerOne.setEnergyPool(new EnumMap<>(EnergyType.class));

        playerTwo.setTeam(new Fighter[]{newFighter(100), newFighter(100), newFighter(100)});
        playerTwo.setEnergyPool(new EnumMap<>(EnergyType.class));

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(1)
                .battleState(BattleState.IN_BATTLE)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");
        activeMatches.put(playerOne.getUsername(), match);
        activeMatches.put(playerTwo.getUsername(), match);

        matchService.endTurn("goku", new TurnActions(List.of()));

        assertSame(playerTwo, match.getCurrentPlayer());
        assertEquals(2, match.getTurnNumber());
        assertEquals(3, playerOne.getEnergyPool().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void searchForMatchShouldOnlyPairPlayersWithSameQueueType() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player goku = newPlayer("goku");
        Player vegeta = newPlayer("vegeta");
        Player gohan = newPlayer("gohan");

        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(goku.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(vegeta.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("gohan")).thenReturn(gohan.getUserProfile());

        List<Long> teamIds = List.of(1L, 2L, 3L);
        matchService.searchForMatch("goku", teamIds, BattleQueueType.LADDER);
        matchService.searchForMatch("vegeta", teamIds, BattleQueueType.QUICK);

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");
        @SuppressWarnings("unchecked")
        Queue<Player> waitingPlayers = (Queue<Player>) ReflectionTestUtils.getField(matchService, "waitingPlayers");
        @SuppressWarnings("unchecked")
        Map<String, BattleQueueType> playerQueueTypes = (Map<String, BattleQueueType>) ReflectionTestUtils.getField(matchService, "playerQueueTypes");

        assertTrue(activeMatches.isEmpty());
        assertEquals(2, waitingPlayers.size());

        matchService.searchForMatch("gohan", teamIds, BattleQueueType.LADDER);

        assertTrue(activeMatches.containsKey("goku"));
        assertTrue(activeMatches.containsKey("gohan"));
        assertFalse(activeMatches.containsKey("vegeta"));
        assertEquals(1, waitingPlayers.size());
        assertEquals(BattleQueueType.QUICK, playerQueueTypes.get("vegeta"));

        Match matched = activeMatches.get("goku");
        assertNotNull(matched);
        assertEquals(1, matched.getPlayerOne().getEnergyPool().values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(3, matched.getPlayerTwo().getEnergyPool().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void searchForMatchShouldRejectQueueWhenRedisHasActiveMatch() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");

        Match redisMatch = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(4)
                .battleState(BattleState.IN_BATTLE)
                .battleQueueType(BattleQueueType.QUICK)
                .build();

        Mockito.when(context.valueOperations().get("match:goku")).thenReturn(redisMatch);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> matchService.searchForMatch("goku", List.of(1L, 2L, 3L), BattleQueueType.QUICK)
        );

        @SuppressWarnings("unchecked")
        Queue<Player> waitingPlayers = (Queue<Player>) ReflectionTestUtils.getField(matchService, "waitingPlayers");

        assertEquals("You are already in a match.", exception.getMessage());
        assertNotNull(waitingPlayers);
        assertTrue(waitingPlayers.isEmpty());
    }

    @Test
    void cancelSearchShouldRemovePlayerFromQueueAndQueueTypeRegistry() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player goku = newPlayer("goku");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(goku.getUserProfile());

        matchService.searchForMatch("goku", List.of(1L, 2L, 3L), BattleQueueType.QUICK);

        boolean cancelled = matchService.cancelSearch("goku");
        boolean cancelledAgain = matchService.cancelSearch("goku");

        @SuppressWarnings("unchecked")
        Queue<Player> waitingPlayers = (Queue<Player>) ReflectionTestUtils.getField(matchService, "waitingPlayers");
        @SuppressWarnings("unchecked")
        Map<String, BattleQueueType> playerQueueTypes = (Map<String, BattleQueueType>) ReflectionTestUtils.getField(matchService, "playerQueueTypes");

        assertTrue(cancelled);
        assertFalse(cancelledAgain);
        assertTrue(waitingPlayers.isEmpty());
        assertNull(playerQueueTypes.get("goku"));
    }

    @Test
    void forfeitShouldEndMatchAndDeclareOpponentWinner() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());

        playerOne.setTeam(new Fighter[]{newFighter(100), newFighter(100), newFighter(100)});
        playerTwo.setTeam(new Fighter[]{newFighter(100), newFighter(100), newFighter(100)});

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(1)
                .battleState(BattleState.IN_BATTLE)
                .battleQueueType(BattleQueueType.QUICK)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");
        activeMatches.put(playerOne.getUsername(), match);
        activeMatches.put(playerTwo.getUsername(), match);

        Mockito.when(context.matchLogRepository().save(any(MatchLog.class)))
                .thenAnswer(invocation -> {
                    MatchLog matchLog = invocation.getArgument(0, MatchLog.class);
                    matchLog.setId(77L);
                    matchLog.setBattleDate(LocalDateTime.now());
                    return matchLog;
                });

        matchService.forfeitMatch("goku");

        assertFalse(activeMatches.containsKey("goku"));
        assertFalse(activeMatches.containsKey("vegeta"));

        ArgumentCaptor<MatchLogDTO> payloadCaptor = ArgumentCaptor.forClass(MatchLogDTO.class);
        verify(context.messagingTemplate(), atLeast(1))
                .convertAndSendToUser(eq("goku"), eq("/queue/match-end"), payloadCaptor.capture());

        MatchLogDTO payload = payloadCaptor.getValue();
        assertNotNull(payload);
        assertEquals("vegeta", payload.winner().username());
        verify(context.userService()).recordMatchResult("goku", "vegeta", "vegeta");
    }

    @Test
    void endTurnShouldHydrateMatchFromRedisWhenActiveCacheIsEmpty() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());

        Fighter attacker = newFighter(100);
        attacker.setSkills(new Skill[]{newSkill(15, List.of(cost(EnergyType.KI, 1)))});
        playerOne.setTeam(new Fighter[]{attacker, newFighter(100), newFighter(100)});
        playerOne.setEnergyPool(new EnumMap<>(EnergyType.class));
        playerOne.getEnergyPool().put(EnergyType.KI, 1);

        Fighter defender = newFighter(100);
        playerTwo.setTeam(new Fighter[]{defender, newFighter(100), newFighter(100)});
        playerTwo.setEnergyPool(new EnumMap<>(EnergyType.class));

        Match redisMatch = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(5)
                .battleState(BattleState.IN_BATTLE)
                .build();

        Mockito.when(context.valueOperations().get("match:goku")).thenReturn(redisMatch);

        matchService.endTurn("goku", new TurnActions(List.of(new TurnAction(0, 0, List.of(0), null))));

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");

        assertEquals(85, defender.getCurrentHp());
        assertTrue(activeMatches.containsKey("goku"));
        assertTrue(activeMatches.containsKey("vegeta"));
        assertEquals("vegeta", redisMatch.getCurrentPlayer().getUsername());
        assertEquals(6, redisMatch.getTurnNumber());
    }

    @Test
    void endTurnShouldPrepareNextPlayersStatusesExactlyOnce() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());

        playerOne.setTeam(new Fighter[]{newFighter(100), newFighter(100), newFighter(100)});
        playerOne.setEnergyPool(new EnumMap<>(EnergyType.class));

        Fighter preparedFighter = newFighter(100);
        preparedFighter.setInvulnerable(true);
        preparedFighter.setInvulnerableTurns(1);
        preparedFighter.setCurrentDamageReduction(10);
        preparedFighter.setDamageReductionTurns(1);
        preparedFighter.setCurrentBonusDamage(15);
        preparedFighter.setBonusDamageTurns(1);
        preparedFighter.setActiveEffects(new ArrayList<>(List.of(
                new ActiveEffect("Barrier", "temporary shield", "barrier.png", 1, false, false, "goku")
        )));
        playerTwo.setTeam(new Fighter[]{preparedFighter, newFighter(100), newFighter(100)});
        playerTwo.setEnergyPool(new EnumMap<>(EnergyType.class));

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(2)
                .battleState(BattleState.IN_BATTLE)
                .currentTurnPrepared(true)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");
        activeMatches.put(playerOne.getUsername(), match);
        activeMatches.put(playerTwo.getUsername(), match);

        matchService.endTurn("goku", new TurnActions(List.of()));

        assertSame(playerTwo, match.getCurrentPlayer());
        assertTrue(match.isCurrentTurnPrepared());
        assertFalse(preparedFighter.isInvulnerable());
        assertEquals(0, preparedFighter.getCurrentDamageReduction());
        assertEquals(0, preparedFighter.getCurrentBonusDamage());
        assertEquals(0, preparedFighter.getActiveEffects().size());
    }

    @Test
    void getMatchShouldIgnoreRedisEntryWhenRequesterIsNotAParticipant() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");

        Match redisMatch = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(2)
                .battleState(BattleState.IN_BATTLE)
                .build();

        Mockito.when(context.valueOperations().get("match:trunks")).thenReturn(redisMatch);

        MatchInfoDTO matchInfo = matchService.getMatch("trunks");

        assertNull(matchInfo);
        verify(context.messagingTemplate(), Mockito.never()).convertAndSendToUser(eq("trunks"), eq("/queue/match"), any());
        Mockito.verify(context.valueOperations(), Mockito.never()).set(eq("match:goku"), any());
    }

    @Test
    void forfeitShouldRejectRedisEntryWhenRequesterIsNotAParticipant() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");

        Match redisMatch = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(3)
                .battleState(BattleState.IN_BATTLE)
                .build();

        Mockito.when(context.valueOperations().get("match:trunks")).thenReturn(redisMatch);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> matchService.forfeitMatch("trunks")
        );

        assertEquals("Match not found.", exception.getMessage());
        Mockito.verify(context.redisTemplate()).delete("match:trunks");
        verify(context.userService(), Mockito.never()).recordMatchResult(any(), any(), any());
    }

    @Test
    void endTurnShouldNormalizeLegacyKrillinDestructoDiscFromCachedMatch() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());

        Character krillin = new Character();
        krillin.setName("Early Krillin");

        Ability legacyDestructoDisc = Ability.builder()
                .name("Destructo Disc")
                .damage(20)
                .damageType(DamageType.PIERCING)
                .effectType(EffectType.NONE)
                .cooldown(1)
                .isHarmful(false)
                .cost(List.of(cost(EnergyType.KI, 1)))
                .build();

        Skill legacySkill = new Skill();
        legacySkill.setAbility(legacyDestructoDisc);
        legacySkill.setCurrentCooldown(0);

        Fighter attacker = newFighter(100);
        attacker.setCharacter(krillin);
        attacker.setSkills(new Skill[]{legacySkill});

        playerOne.setTeam(new Fighter[]{attacker, newFighter(100), newFighter(100)});
        playerOne.setEnergyPool(new EnumMap<>(EnergyType.class));
        playerOne.getEnergyPool().put(EnergyType.KI, 1);

        Fighter defender = newFighter(100);
        playerTwo.setTeam(new Fighter[]{defender, newFighter(100), newFighter(100)});
        playerTwo.setEnergyPool(new EnumMap<>(EnergyType.class));

        Match redisMatch = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(2)
                .battleState(BattleState.IN_BATTLE)
                .build();

        Mockito.when(context.valueOperations().get("match:goku")).thenReturn(redisMatch);

        matchService.endTurn("goku", new TurnActions(List.of(new TurnAction(0, 0, List.of(0), null))));

        assertEquals(80, defender.getCurrentHp());
        assertTrue(Boolean.TRUE.equals(legacyDestructoDisc.getIsHarmful()));
    }

    @Test
    void stunnedFighterActionShouldBeRejectedWithoutAdvancingTurn() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());

        Fighter stunnedAttacker = newFighter(100);
        stunnedAttacker.setStunned(true);
        stunnedAttacker.setStunTurns(1);
        stunnedAttacker.setSkills(new Skill[]{newSkill(45, List.of(cost(EnergyType.KI, 1)))});

        playerOne.setTeam(new Fighter[]{stunnedAttacker, newFighter(100), newFighter(100)});
        playerOne.setEnergyPool(new EnumMap<>(EnergyType.class));
        playerOne.getEnergyPool().put(EnergyType.KI, 1);

        Fighter defender = newFighter(100);
        playerTwo.setTeam(new Fighter[]{defender, newFighter(100), newFighter(100)});
        playerTwo.setEnergyPool(new EnumMap<>(EnergyType.class));

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(2)
                .battleState(BattleState.IN_BATTLE)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");
        activeMatches.put(playerOne.getUsername(), match);
        activeMatches.put(playerTwo.getUsername(), match);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> matchService.endTurn("goku", new TurnActions(List.of(new TurnAction(0, 0, List.of(0), null))))
        );

        assertEquals("Invalid actions.", exception.getMessage());
        assertEquals(100, defender.getCurrentHp());
        assertTrue(stunnedAttacker.isStunned());
        assertEquals(1, stunnedAttacker.getStunTurns());
        assertEquals("goku", match.getCurrentPlayer().getUsername());
    }

    @Test
    void endTurnShouldRejectInvalidTurnAtomically() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());

        Fighter firstAttacker = newFighter(100);
        firstAttacker.setSkills(new Skill[]{newSkill(20, List.of(cost(EnergyType.KI, 1)))});
        Fighter secondAttacker = newFighter(100);
        secondAttacker.setSkills(new Skill[]{newSkill(20, List.of(cost(EnergyType.KI, 1)))});
        playerOne.setTeam(new Fighter[]{firstAttacker, secondAttacker, newFighter(100)});
        playerOne.setEnergyPool(new EnumMap<>(EnergyType.class));
        playerOne.getEnergyPool().put(EnergyType.KI, 2);

        Fighter defender = newFighter(100);
        playerTwo.setTeam(new Fighter[]{defender, newFighter(100), newFighter(100)});
        playerTwo.setEnergyPool(new EnumMap<>(EnergyType.class));

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(4)
                .battleState(BattleState.IN_BATTLE)
                .currentTurnPrepared(true)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");
        activeMatches.put(playerOne.getUsername(), match);
        activeMatches.put(playerTwo.getUsername(), match);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> matchService.endTurn("goku", new TurnActions(List.of(
                        new TurnAction(0, 0, List.of(0), null),
                        new TurnAction(1, 0, List.of(99), null)
                )))
        );

        assertEquals("Invalid actions.", exception.getMessage());
        assertEquals(100, defender.getCurrentHp());
        assertSame(playerOne, match.getCurrentPlayer());
        assertEquals(4, match.getTurnNumber());
        assertEquals(2, playerOne.getEnergyPool().getOrDefault(EnergyType.KI, 0));
    }

    @Test
    void endTurnShouldExpandAoeTargetsAroundSelectedPrimaryTarget() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("vegeta");
        Player playerTwo = newPlayer("goku");
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerTwo.getUserProfile());

        Fighter attacker = newFighter(100);
        Character vegetaCharacter = new Character();
        vegetaCharacter.setName("Early Vegeta");
        attacker.setCharacter(vegetaCharacter);

        Skill galickGun = newSkill(30, List.of(cost(EnergyType.KI, 1), cost(EnergyType.ANY, 1)));
        galickGun.getAbility().setName("Galick Gun");
        galickGun.getAbility().setEffectType(EffectType.AOE);
        galickGun.getAbility().setSecondaryDamage(null);
        attacker.setSkills(new Skill[]{galickGun});

        playerOne.setTeam(new Fighter[]{attacker, newFighter(100), newFighter(100)});
        playerOne.setEnergyPool(new EnumMap<>(EnergyType.class));
        playerOne.getEnergyPool().put(EnergyType.KI, 1);
        playerOne.getEnergyPool().put(EnergyType.COMBAT, 1);

        Fighter defenderOne = newFighter(100);
        Fighter defenderTwo = newFighter(100);
        Fighter defenderThree = newFighter(100);
        playerTwo.setTeam(new Fighter[]{defenderOne, defenderTwo, defenderThree});
        playerTwo.setEnergyPool(new EnumMap<>(EnergyType.class));

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(2)
                .battleState(BattleState.IN_BATTLE)
                .currentTurnPrepared(true)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");
        activeMatches.put(playerOne.getUsername(), match);
        activeMatches.put(playerTwo.getUsername(), match);

        matchService.endTurn("vegeta", new TurnActions(List.of(
                new TurnAction(0, 0, List.of(1), null)
        )));

        assertEquals(90, defenderOne.getCurrentHp());
        assertEquals(70, defenderTwo.getCurrentHp());
        assertEquals(90, defenderThree.getCurrentHp());
    }

    @Test
    void endTurnShouldPublishMatchEndPayloadWithResolvedProfiles() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Mockito.when(context.userService().getPublicProfile("goku")).thenReturn(playerOne.getUserProfile());
        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());

        Fighter attacker = newFighter(100);
        attacker.setSkills(new Skill[]{newSkill(50, List.of(cost(EnergyType.KI, 1)))});
        playerOne.setTeam(new Fighter[]{attacker, newFighter(100), newFighter(100)});
        playerOne.setEnergyPool(new EnumMap<>(EnergyType.class));
        playerOne.getEnergyPool().put(EnergyType.KI, 1);

        Fighter defender = newFighter(30);
        playerTwo.setTeam(new Fighter[]{defender, newFighter(0), newFighter(0)});
        playerTwo.getTeam()[1].setAlive(false);
        playerTwo.getTeam()[2].setAlive(false);
        playerTwo.setEnergyPool(new EnumMap<>(EnergyType.class));

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(3)
                .battleState(BattleState.IN_BATTLE)
                .battleQueueType(BattleQueueType.QUICK)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Match> activeMatches = (Map<String, Match>) ReflectionTestUtils.getField(matchService, "activeMatches");
        activeMatches.put(playerOne.getUsername(), match);
        activeMatches.put(playerTwo.getUsername(), match);

        Mockito.when(context.matchLogRepository().save(any(MatchLog.class)))
                .thenAnswer(invocation -> {
                    MatchLog matchLog = invocation.getArgument(0, MatchLog.class);
                    matchLog.setId(99L);
                    matchLog.setBattleDate(LocalDateTime.now());
                    return matchLog;
                });

        matchService.endTurn("goku", new TurnActions(List.of(new TurnAction(0, 0, List.of(0), null))));

        ArgumentCaptor<MatchLogDTO> payloadCaptor = ArgumentCaptor.forClass(MatchLogDTO.class);
        verify(context.messagingTemplate(), atLeast(1)).convertAndSendToUser(eq("goku"), eq("/queue/match-end"), payloadCaptor.capture());

        MatchLogDTO payload = payloadCaptor.getValue();
        assertNotNull(payload);
        assertEquals("goku", payload.winner().username());
        assertEquals("goku", payload.playerOne().username());
        assertEquals("vegeta", payload.playerTwo().username());
        verify(context.userService()).recordMatchResult("goku", "vegeta", "goku");
    }

    @Test
    void getMatchShouldHydrateRedisPayloadAndRefreshCanonicalCache() {
        TestContext context = newTestContext();
        MatchService matchService = context.matchService();

        Player playerOne = newPlayer("goku");
        Player playerTwo = newPlayer("vegeta");
        Fighter currentTurnFighter = newFighter(100);
        currentTurnFighter.setInvulnerable(true);
        currentTurnFighter.setInvulnerableTurns(1);
        currentTurnFighter.setCurrentDamageReduction(10);
        currentTurnFighter.setDamageReductionTurns(1);
        currentTurnFighter.setActiveEffects(new ArrayList<>(List.of(
                new ActiveEffect("Afterimage", "legacy prepared state", "afterimage.png", 1, false, false, "goku")
        )));
        playerOne.setTeam(new Fighter[]{currentTurnFighter, newFighter(100), newFighter(100)});
        playerTwo.setTeam(new Fighter[]{newFighter(100), newFighter(100), newFighter(100)});

        Match redisMatch = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(7)
                .battleState(BattleState.IN_BATTLE)
                .battleQueueType(BattleQueueType.QUICK)
                .build();

        Map<String, Object> cachedPayload = context.objectMapper().convertValue(
                redisMatch,
                new TypeReference<>() {
                }
        );

        Mockito.when(context.userService().getPublicProfile("vegeta")).thenReturn(playerTwo.getUserProfile());
        Mockito.when(context.valueOperations().get("match:goku")).thenReturn(cachedPayload);

        MatchInfoDTO matchInfo = matchService.getMatch("goku");

        assertNotNull(matchInfo);
        assertNotNull(matchInfo.match());
        assertEquals(7, matchInfo.match().getTurnNumber());
        assertEquals("vegeta", matchInfo.opponentData().username());
        assertTrue(matchInfo.match().isCurrentTurnPrepared());
        Fighter hydratedCurrentTurnFighter = matchInfo.match().getPlayerOne().getTeam()[0];
        assertFalse(hydratedCurrentTurnFighter.isInvulnerable());
        assertEquals(0, hydratedCurrentTurnFighter.getCurrentDamageReduction());
        assertEquals(0, hydratedCurrentTurnFighter.getActiveEffects().size());

        verify(context.valueOperations()).set(eq("match:goku"), any(Match.class));
        verify(context.valueOperations()).set(eq("match:vegeta"), any(Match.class));
    }

    private static TestContext newTestContext() {
        SimpMessagingTemplate messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> redisTemplate = (RedisTemplate<String, Object>) Mockito.mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        UserService userService = Mockito.mock(UserService.class);
        AbilityRepository abilityRepository = Mockito.mock(AbilityRepository.class);
        CharacterRepository characterRepository = Mockito.mock(CharacterRepository.class);
        MatchLogRepository matchLogRepository = Mockito.mock(MatchLogRepository.class);
        MatchLogMapper matchLogMapper = Mockito.mock(MatchLogMapper.class);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(characterRepository.findAllByIdIn(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    List<Long> ids = invocation.getArgument(0, List.class);
                    List<Character> characters = new ArrayList<>();
                    for (Long id : ids) {
                        Character character = new Character();
                        character.setId(id);
                        character.setName("character-" + id);
                        character.setAbilities(List.of());
                        characters.add(character);
                    }
                    return characters;
                });
                Mockito.when(abilityRepository.findAllByIdIn(Mockito.anyList())).thenReturn(List.of());
                Mockito.when(matchLogMapper.toDTO(any(MatchLog.class)))
                    .thenAnswer(invocation -> {
                        MatchLog matchLog = invocation.getArgument(0, MatchLog.class);
                        return new MatchLogDTO(
                            matchLog.getId(),
                            null,
                            null,
                            null,
                            matchLog.getBattleQueueType(),
                            matchLog.getBattleDate()
                        );
                    });

        MatchService matchService = new MatchService(
                messagingTemplate,
                redisTemplate,
                userService,
                characterRepository,
                abilityRepository,
                matchLogRepository,
                matchLogMapper,
                objectMapper
        );

        return new TestContext(matchService, userService, redisTemplate, valueOperations, matchLogRepository, messagingTemplate, objectMapper);
    }

    private record TestContext(
            MatchService matchService,
            UserService userService,
            RedisTemplate<String, Object> redisTemplate,
            ValueOperations<String, Object> valueOperations,
            MatchLogRepository matchLogRepository,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper
    ) {
    }

    private static Player newPlayer(String username) {
        return new Player(new PublicProfileDTO(
                username,
                1,
                "",
                Role.MEMBER,
                Rank.BABY,
                "avatar",
                1,
                1,
                0L,
                0,
                0,
                0,
                0,
                LocalDateTime.now()
        ));
    }

    private static Fighter newFighter(int hp) {
        Fighter fighter = new Fighter();
        fighter.setCurrentHp(hp);
        fighter.setCurrentDestructibleDefense(0);
        fighter.setCurrentDamageReduction(0);
        fighter.setCurrentBonusDamage(0);
        fighter.setAlive(true);
        fighter.setStunned(false);
        fighter.setInvulnerable(false);
        fighter.setSkills(new Skill[0]);
        return fighter;
    }

    private static Skill newSkill(int damage, List<AbilityCost> costs) {
        Ability ability = Ability.builder()
                .name("Ki Blast")
                .description("Test")
                .damage(damage)
                .damageType(DamageType.FLAT)
                .effectType(EffectType.NONE)
                .distance(Distance.RANGED)
                .isHarmful(true)
                .cooldown(1)
                .cost(costs)
                .build();
        Skill skill = new Skill();
        skill.setAbility(ability);
        skill.setCurrentCooldown(0);
        return skill;
    }

    private static AbilityCost cost(EnergyType type, int amount) {
        AbilityCost cost = new AbilityCost();
        cost.setEnergyType(type);
        cost.setAmount(amount);
        return cost;
    }
}
