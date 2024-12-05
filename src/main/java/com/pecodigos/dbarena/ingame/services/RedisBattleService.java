package com.pecodigos.dbarena.ingame.services;

import com.pecodigos.dbarena.ingame.models.BattleRoom;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisBattleService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BATTLE_STATE = "battleState:";

    public RedisBattleService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Save battle state to Redis
    public void saveBattleState(String gameId, BattleRoom battleRoom) {
        // Serialize the BattleRoom object as JSON
        redisTemplate.opsForValue().set(BATTLE_STATE + gameId, battleRoom);
    }

    // Retrieve battle state from Redis
    public BattleRoom getBattleState(String gameId) {
        // Deserialize the JSON back into a BattleRoom object
        return (BattleRoom) redisTemplate.opsForValue().get(BATTLE_STATE + gameId);
    }

    public void setBattleStateExpiration(String gameId, Duration duration) {
        redisTemplate.expire(BATTLE_STATE + gameId, duration);
    }

    public void updateCharacterHp(String gameId, String characterId, int newHp) {
        BattleRoom battleRoom = getBattleState(gameId);
        if (battleRoom == null) {
            throw new IllegalStateException("BattleRoom not found for gameId: " + gameId);
        }

        // Locate the character and update its HP
        battleRoom.getPlayerOne().getTeam().stream()
                .filter(fighter -> fighter.getId().equals(Long.parseLong(characterId)))
                .findFirst()
                .ifPresent(fighter -> fighter.setCurrentHp(newHp));

        battleRoom.getPlayerTwo().getTeam().stream()
                .filter(fighter -> fighter.getId().equals(Long.parseLong(characterId)))
                .findFirst()
                .ifPresent(fighter -> fighter.setCurrentHp(newHp));

        // Save updated state
        saveBattleState(gameId, battleRoom);
    }
}
