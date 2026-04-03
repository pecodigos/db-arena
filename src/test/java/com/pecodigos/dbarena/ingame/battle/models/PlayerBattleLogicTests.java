package com.pecodigos.dbarena.ingame.battle.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pecodigos.dbarena.ingame.entities.Ability;
import com.pecodigos.dbarena.ingame.entities.AbilityCost;
import com.pecodigos.dbarena.ingame.entities.Character;
import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import com.pecodigos.dbarena.ingame.enums.skills.DamageType;
import com.pecodigos.dbarena.ingame.enums.skills.EffectType;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.enums.Rank;
import com.pecodigos.dbarena.user.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerBattleLogicTests {

    @Test
    void flatDamageShouldReduceCurrentHpAfterMitigation() {
        Player attackerOwner = newPlayer();
        Fighter attacker = newFighter(100, 0, 0);
        attacker.setCurrentBonusDamage(0);

        Fighter target = newFighter(100, 10, 5);
        Skill skill = newSkill(45, DamageType.FLAT, EffectType.NONE, true, List.of());

        attackerOwner.useHarmfulSkill(attacker, new Fighter[]{target}, skill, null, newPlayer("vegeta"));

        assertEquals(70, target.getCurrentHp());
        assertEquals(0, target.getCurrentDestructibleDefense());
        assertEquals(5, target.getCurrentDamageReduction());
    }

    @Test
    void helpfulSkillWithEnoughEnergyShouldBeUsable() {
        Player player = newPlayer();
        player.setEnergyPool(new EnumMap<>(EnergyType.class));
        player.getEnergyPool().put(EnergyType.COMBAT, 1);

        Fighter target = newFighter(50, 0, 0);
        Skill heal = newSkill(0, DamageType.NONE, EffectType.HEAL, false, List.of(cost(EnergyType.COMBAT, 1)));
        heal.getAbility().setHelpingPoints(20);

        assertDoesNotThrow(() -> player.useHelpfulSkill(new Fighter[]{target}, heal, null));
        assertEquals(70, target.getCurrentHp());
        assertEquals(0, player.getEnergyPool().getOrDefault(EnergyType.COMBAT, 0));
    }

    @Test
    void invulnerableSkillShouldRegisterActiveEffectOnTarget() {
        Player player = newPlayer();
        player.setEnergyPool(new EnumMap<>(EnergyType.class));
        player.getEnergyPool().put(EnergyType.KI, 1);

        Fighter target = newFighter(100, 0, 0);
        Skill invulnerable = newSkill(0, DamageType.NONE, EffectType.INVULNERABLE, false, List.of(cost(EnergyType.KI, 1)));
        invulnerable.getAbility().setDurationInTurns(1);
        invulnerable.getAbility().setImagePath("invuln.png");

        player.useHelpfulSkill(new Fighter[]{target}, invulnerable, null);

        assertEquals(1, target.getActiveEffects().size());
        assertEquals("test", target.getActiveEffects().get(0).getName());
        assertEquals(1, target.getActiveEffects().get(0).getRemainingTurns());
    }

    @Test
    void wildcardEnergyCostShouldUseAvailableSpecificEnergy() {
        Player player = newPlayer();
        player.setEnergyPool(new EnumMap<>(EnergyType.class));
        player.getEnergyPool().put(EnergyType.KI, 1);

        Fighter target = newFighter(60, 0, 0);
        Skill heal = newSkill(0, DamageType.NONE, EffectType.HEAL, false, List.of(cost(EnergyType.ANY, 1)));
        heal.getAbility().setHelpingPoints(10);

        assertDoesNotThrow(() -> player.useHelpfulSkill(new Fighter[]{target}, heal, null));
        assertEquals(70, target.getCurrentHp());
        assertEquals(0, player.getEnergyPool().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void generateEnergyShouldNeverCreateAnyOrNoneEnergy() {
        Player player = newPlayer();
        player.setEnergyPool(new EnumMap<>(EnergyType.class));
        player.setTeam(new Fighter[]{newFighter(100, 0, 0), newFighter(0, 0, 0), newFighter(0, 0, 0)});
        player.getTeam()[1].setAlive(false);
        player.getTeam()[2].setAlive(false);
        player.setFirstTurn(true);
        player.setRandom(new Random() {
            @Override
            public int nextInt(int bound) {
                return bound - 1;
            }
        });

        player.generateEnergy();

        assertEquals(1, player.getEnergyPool().values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(0, player.getEnergyPool().getOrDefault(EnergyType.ANY, 0));
        assertEquals(0, player.getEnergyPool().getOrDefault(EnergyType.NONE, 0));
    }

    @Test
    void invulnerableTargetShouldIgnoreIncomingDamage() {
        Player player = newPlayer();
        player.setEnergyPool(new EnumMap<>(EnergyType.class));
        player.getEnergyPool().put(EnergyType.KI, 1);

        Fighter attacker = newFighter(100, 0, 0);
        Fighter target = newFighter(100, 0, 0);
        target.setInvulnerable(true);
        target.setInvulnerableTurns(1);

        Skill attack = newSkill(30, DamageType.FLAT, EffectType.NONE, true, List.of(cost(EnergyType.KI, 1)));

        player.useHarmfulSkill(attacker, new Fighter[]{target}, attack, null, newPlayer("vegeta"));

        assertEquals(100, target.getCurrentHp());
        assertEquals(true, target.isInvulnerable());
    }

    @Test
    void prepareForTurnShouldExpireTemporaryDefensiveStatuses() {
        Player player = newPlayer();
        Fighter fighter = newFighter(100, 0, 15);
        fighter.setInvulnerable(true);
        fighter.setInvulnerableTurns(1);
        fighter.setDamageReductionTurns(1);
        fighter.setCurrentBonusDamage(10);
        fighter.setBonusDamageTurns(1);
        player.setTeam(new Fighter[]{fighter, newFighter(100, 0, 0), newFighter(100, 0, 0)});

        player.prepareForTurn();

        assertEquals(false, fighter.isInvulnerable());
        assertEquals(0, fighter.getCurrentDamageReduction());
        assertEquals(0, fighter.getCurrentBonusDamage());
    }

    @Test
    void prepareForTurnShouldDecreaseActiveEffectTurns() {
        Player player = newPlayer();
        Fighter fighter = newFighter(100, 0, 0);
        fighter.setActiveEffects(new java.util.ArrayList<>(List.of(new ActiveEffect("dot", "Some dot desc", "dot.png", 2, true, false, "Unknown"))));
        player.setTeam(new Fighter[]{fighter, newFighter(100, 0, 0), newFighter(100, 0, 0)});

        player.prepareForTurn();

        assertEquals(1, fighter.getActiveEffects().size());
        assertEquals(1, fighter.getActiveEffects().get(0).getRemainingTurns());

        player.prepareForTurn();
        assertEquals(0, fighter.getActiveEffects().size());
    }

    @Test
    void harmfulSkillShouldRequireEnoughEnergy() {
        Player player = newPlayer();
        Fighter attacker = newFighter(100, 0, 0);
        Fighter target = newFighter(100, 0, 0);
        Skill attack = newSkill(30, DamageType.FLAT, EffectType.NONE, true, List.of(cost(EnergyType.KI, 1)));

        assertThrows(IllegalStateException.class, () -> player.useHarmfulSkill(attacker, new Fighter[]{target}, attack, null, newPlayer("vegeta")));
    }

    @Test
    void weakenShouldReduceDamageOnTargetsNextTurnBeforeExpiring() {
        Player goku = newPlayer("goku");
        Player vegeta = newPlayer("vegeta");
        Fighter gokuAttacker = newFighter(100, 0, 0);
        Fighter weakenedVegeta = newFighter(100, 0, 0);
        vegeta.setTeam(new Fighter[]{weakenedVegeta, newFighter(100, 0, 0), newFighter(100, 0, 0)});

        Skill rushAttack = newSkill(0, DamageType.NONE, EffectType.WEAKEN, true, List.of());
        rushAttack.getAbility().setName("Rush Attack");
        rushAttack.getAbility().setHelpingPoints(10);
        rushAttack.getAbility().setDurationInTurns(1);

        goku.useHarmfulSkill(gokuAttacker, new Fighter[]{weakenedVegeta}, rushAttack, null, vegeta);

        assertEquals(10, weakenedVegeta.getWeaknessAmount());
        assertEquals(1, weakenedVegeta.getWeaknessTurns());
        assertEquals(1, weakenedVegeta.getActiveEffects().size());

        vegeta.prepareForTurn();

        Fighter victim = newFighter(100, 0, 0);
        Player victimOwner = newPlayer("krillin");
        victimOwner.setTeam(new Fighter[]{victim, newFighter(100, 0, 0), newFighter(100, 0, 0)});
        Skill blast = newSkill(20, DamageType.FLAT, EffectType.NONE, true, List.of());

        vegeta.useHarmfulSkill(weakenedVegeta, new Fighter[]{victim}, blast, null, victimOwner);

        assertEquals(90, victim.getCurrentHp());

        vegeta.reduceCooldowns();

        assertEquals(0, weakenedVegeta.getWeaknessAmount());
        assertTrue(weakenedVegeta.getActiveEffects().isEmpty());
    }

    @Test
    void protectAllyShouldSplitDamageWithPiccoloAndExpireOnOwnersNextTurn() {
        Player piccoloOwner = newPlayer("goku");
        Fighter piccolo = newFighter(100, 0, 0);
        Character piccoloCharacter = new Character();
        piccoloCharacter.setName("Early Piccolo");
        piccolo.setCharacter(piccoloCharacter);

        Fighter ally = newFighter(100, 0, 0);
        piccoloOwner.setTeam(new Fighter[]{piccolo, ally, newFighter(100, 0, 0)});

        Skill protectAlly = newSkill(0, DamageType.NONE, EffectType.PREPARATION, false, List.of());
        protectAlly.getAbility().setName("Protect an Ally");
        protectAlly.getAbility().setDurationInTurns(1);
        protectAlly.getAbility().setIsInvisible(true);
        piccolo.setSkills(new Skill[]{protectAlly});

        piccoloOwner.useHelpfulSkill(new Fighter[]{ally}, protectAlly, null);

        assertEquals(1, ally.getActiveEffects().size());
        assertTrue(ally.getActiveEffects().get(0).isInvisible());

        Player attackerOwner = newPlayer("vegeta");
        Fighter attacker = newFighter(100, 0, 0);
        Skill blast = newSkill(30, DamageType.FLAT, EffectType.NONE, true, List.of());

        attackerOwner.useHarmfulSkill(attacker, new Fighter[]{ally}, blast, null, piccoloOwner);

        assertEquals(85, ally.getCurrentHp());
        assertEquals(85, piccolo.getCurrentHp());

        piccoloOwner.prepareForTurn();

        assertTrue(ally.getActiveEffects().isEmpty());
    }

    @Test
    void destructoDiscShouldDealItsFollowUpDamageOnFutureTurn() {
        Player krillin = newPlayer("krillin");
        Player defenderOwner = newPlayer("goku");
        Fighter attacker = newFighter(100, 0, 0);
        Fighter target = newFighter(100, 0, 0);
        defenderOwner.setTeam(new Fighter[]{target, newFighter(100, 0, 0), newFighter(100, 0, 0)});

        Skill destructoDisc = newSkill(20, DamageType.PIERCING, EffectType.DOT, true, List.of());
        destructoDisc.getAbility().setName("Destructo Disc");
        destructoDisc.getAbility().setDurationInTurns(2);

        krillin.useHarmfulSkill(attacker, new Fighter[]{target}, destructoDisc, null, defenderOwner);

        assertEquals(80, target.getCurrentHp());
        assertEquals(1, target.getActiveEffects().size());
        assertEquals(1, target.getActiveEffects().get(0).getRemainingTurns());

        defenderOwner.prepareForTurn();

        assertEquals(60, target.getCurrentHp());
        assertTrue(target.getActiveEffects().isEmpty());
    }

    @Test
    void aoeSkillShouldApplySecondaryDamageToNonPrimaryTargets() {
        Player vegeta = newPlayer("vegeta");
        Player defenders = newPlayer("goku");
        Fighter attacker = newFighter(100, 0, 0);
        Fighter primaryTarget = newFighter(100, 0, 0);
        Fighter splashTargetOne = newFighter(100, 0, 0);
        Fighter splashTargetTwo = newFighter(100, 0, 0);
        defenders.setTeam(new Fighter[]{primaryTarget, splashTargetOne, splashTargetTwo});

        Skill galickGun = newSkill(30, DamageType.FLAT, EffectType.AOE, true, List.of());
        galickGun.getAbility().setName("Galick Gun");
        galickGun.getAbility().setSecondaryDamage(10);

        vegeta.useHarmfulSkill(attacker, new Fighter[]{primaryTarget, splashTargetOne, splashTargetTwo}, galickGun, null, defenders);

        assertEquals(70, primaryTarget.getCurrentHp());
        assertEquals(90, splashTargetOne.getCurrentHp());
        assertEquals(90, splashTargetTwo.getCurrentHp());
    }

    @Test
    void matchConversionShouldIgnoreLegacyRandomFieldsInPlayerPayload() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Player playerOne = newPlayer();
        playerOne.setTeam(new Fighter[]{newFighter(100, 0, 0), newFighter(100, 0, 0), newFighter(100, 0, 0)});

        Player playerTwo = newPlayer();
        playerTwo.setTeam(new Fighter[]{newFighter(100, 0, 0), newFighter(100, 0, 0), newFighter(100, 0, 0)});

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerOne)
                .turnNumber(1)
                .battleState(BattleState.IN_BATTLE)
                .build();

        Map<String, Object> serializedMatch = mapper.convertValue(match, new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        Map<String, Object> serializedPlayerOne = (Map<String, Object>) serializedMatch.get("playerOne");
        serializedPlayerOne.put("random", Map.of("seed", 123L, "deprecated", true));

        Match restored = assertDoesNotThrow(() -> mapper.convertValue(serializedMatch, Match.class));
        assertEquals("goku", restored.getPlayerOne().getUsername());
    }

    private static Player newPlayer() {
        return newPlayer("goku");
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

    private static Fighter newFighter(int hp, int destructibleDefense, int damageReduction) {
        Fighter fighter = new Fighter();
        fighter.setCurrentHp(hp);
        fighter.setCurrentDestructibleDefense(destructibleDefense);
        fighter.setCurrentDamageReduction(damageReduction);
        fighter.setCurrentBonusDamage(0);
        fighter.setAlive(true);
        fighter.setStunned(false);
        fighter.setInvulnerable(false);
        fighter.setSkills(new Skill[0]);
        fighter.setActiveEffects(new java.util.ArrayList<>());
        return fighter;
    }

    private static Skill newSkill(int damage, DamageType damageType, EffectType effectType, boolean harmful, List<AbilityCost> costs) {
        Ability ability = Ability.builder()
                .name("test")
                .description("test")
                .damage(damage)
                .damageType(damageType)
                .effectType(effectType)
                .isHarmful(harmful)
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
