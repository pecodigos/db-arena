package com.pecodigos.dbarena.ingame.services;

import com.pecodigos.dbarena.ingame.models.BattlePlayer;
import com.pecodigos.dbarena.ingame.models.BattleRoom;
import com.pecodigos.dbarena.ingame.models.BattleFighter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RedisBattleService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BATTLE_STATE = "battleState:";

    public RedisBattleService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Save battle state to Redis
    public void saveBattleState(String gameId, BattleRoom battleRoom) {
        redisTemplate.opsForValue().set(BATTLE_STATE + gameId, battleRoom);
    }

    // Retrieve battle state from Redis
    public BattleRoom getBattleState(String gameId) {
        return (BattleRoom) redisTemplate.opsForValue().get(BATTLE_STATE + gameId);
    }

    // Set expiration time for the battle state
    public void setBattleStateExpiration(String gameId, Duration duration) {
        redisTemplate.expire(BATTLE_STATE + gameId, duration);
    }

    // Update the HP of a specific character in the battle
    public void updateCharacterHp(String gameId, String characterId, int newHp) {
        BattleRoom battleRoom = getBattleState(gameId);
        if (battleRoom == null) {
            throw new IllegalStateException("BattleRoom not found for gameId: " + gameId);
        }

        // Update the HP of the character in the player's team
        boolean updated = updateCharacterHpInPlayer(battleRoom.getPlayerOne(), characterId, newHp);
        updated = updated || updateCharacterHpInPlayer(battleRoom.getPlayerTwo(), characterId, newHp);

        if (!updated) {
            throw new IllegalArgumentException("Fighter with ID " + characterId + " not found.");
        }

        // Save updated battle state
        saveBattleState(gameId, battleRoom);
    }

    // Helper method to update character HP in a given player
    private boolean updateCharacterHpInPlayer(BattlePlayer player, String characterId, int newHp) {
        List<BattleFighter> updatedTeam = player.getTeam().stream()
                .map(fighter -> {
                    if (String.valueOf(fighter.getId()).equals(characterId)) {
                        fighter.setCurrentHp(newHp);  // Update HP
                    }
                    return fighter;
                })
                .collect(Collectors.toList());

        // If a character's HP was updated, update the team's list
        if (updatedTeam.stream().anyMatch(fighter -> fighter.getId().equals(Long.parseLong(characterId)))) {
            player.setTeam(updatedTeam);
            return true;
        }
        return false;
    }
}
