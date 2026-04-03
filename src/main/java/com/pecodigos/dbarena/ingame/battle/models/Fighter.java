package com.pecodigos.dbarena.ingame.battle.models;

import com.pecodigos.dbarena.ingame.entities.Character;
import com.pecodigos.dbarena.ingame.enums.skills.DamageType;
import com.pecodigos.dbarena.ingame.enums.skills.EffectType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Data
@RequiredArgsConstructor
public class Fighter {
    private Character character;
    private Skill[] skills;
    private Integer currentHp;
    private static final Integer MAX_HP = 100;
    private Integer currentDestructibleDefense;
    private Integer currentDamageReduction;
    private Integer damageReductionTurns;
    private Integer currentBonusDamage;
    private Integer bonusDamageTurns;
    private Integer weaknessAmount;
    private Integer weaknessTurns;
    private boolean isStunned;
    private Integer stunTurns;
    private boolean isUnableToBecomeInvulnerable;
    private boolean isInvulnerable;
    private Integer invulnerableTurns;
    private boolean isAlive;
    private List<ActiveEffect> activeEffects;

    public void tickStartOfTurn() {
        applyDamageOverTimeEffects();

        if (invulnerableTurns != null && invulnerableTurns > 0) {
            invulnerableTurns--;
            if (invulnerableTurns <= 0) {
                isInvulnerable = false;
            }
        }

        if (damageReductionTurns != null && damageReductionTurns > 0) {
            damageReductionTurns--;
            if (damageReductionTurns <= 0) {
                currentDamageReduction = 0;
            }
        }

        if (bonusDamageTurns != null && bonusDamageTurns > 0) {
            bonusDamageTurns--;
            if (bonusDamageTurns <= 0) {
                currentBonusDamage = 0;
            }
        }

        decrementActiveEffects(false);
    }

    public void consumeStunTurn() {
        if (stunTurns == null || stunTurns <= 0) {
            isStunned = false;
            return;
        }

        stunTurns--;
        if (stunTurns <= 0) {
            isStunned = false;
        }
    }

    public void tickEndOfTurn() {
        if (weaknessTurns != null && weaknessTurns > 0) {
            weaknessTurns--;
            if (weaknessTurns <= 0) {
                weaknessAmount = 0;
            }
        }

        decrementActiveEffects(true);
    }

    private void applyDamageOverTimeEffects() {
        if (activeEffects == null || activeEffects.isEmpty()) {
            return;
        }

        for (ActiveEffect activeEffect : activeEffects) {
            if (activeEffect == null || activeEffect.getEffectType() != EffectType.DOT) {
                continue;
            }

            Integer remainingTurns = activeEffect.getRemainingTurns();
            if (remainingTurns == null || remainingTurns <= 0) {
                continue;
            }

            applyEffectDamage(activeEffect);
        }
    }

    private void applyEffectDamage(ActiveEffect activeEffect) {
        int effectDamage = Optional.ofNullable(activeEffect.getDamage()).orElse(0);
        if (effectDamage <= 0 || !isAlive) {
            return;
        }

        DamageType damageType = Optional.ofNullable(activeEffect.getDamageType()).orElse(DamageType.NONE);
        int hpDamage = switch (damageType) {
            case NONE -> 0;
            case AFFLICTION -> effectDamage;
            case FLAT -> {
                int damageReduction = Optional.ofNullable(currentDamageReduction).orElse(0);
                int destructibleDefense = Optional.ofNullable(currentDestructibleDefense).orElse(0);
                currentDestructibleDefense = Math.max(destructibleDefense - effectDamage, 0);
                yield Math.max(0, effectDamage - destructibleDefense - damageReduction);
            }
            case PIERCING -> {
                int destructibleDefense = Optional.ofNullable(currentDestructibleDefense).orElse(0);
                currentDestructibleDefense = Math.max(destructibleDefense - effectDamage, 0);
                yield Math.max(0, effectDamage - destructibleDefense);
            }
        };

        if (hpDamage <= 0) {
            return;
        }

        currentHp = Math.max(0, Optional.ofNullable(currentHp).orElse(0) - hpDamage);
        if (currentHp <= 0) {
            isAlive = false;
        }
    }

    private void decrementActiveEffects(boolean atTurnEnd) {
        if (activeEffects == null || activeEffects.isEmpty()) {
            return;
        }

        Iterator<ActiveEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            ActiveEffect activeEffect = iterator.next();
            Integer remainingTurns = activeEffect.getRemainingTurns();

            if (remainingTurns == null) {
                iterator.remove();
                continue;
            }

            EffectType effectType = Optional.ofNullable(activeEffect.getEffectType()).orElse(EffectType.NONE);
            if (shouldExpireAtTurnEnd(effectType) != atTurnEnd) {
                continue;
            }

            int updatedTurns = remainingTurns - 1;
            activeEffect.setRemainingTurns(updatedTurns);

            if (updatedTurns <= 0) {
                iterator.remove();
            }
        }
    }

    private boolean shouldExpireAtTurnEnd(EffectType effectType) {
        return switch (effectType) {
            case STUN, REFLECT, ENERGY_REMOVAL, ENERGY_DRAIN, WEAKEN -> true;
            default -> false;
        };
    }
}
