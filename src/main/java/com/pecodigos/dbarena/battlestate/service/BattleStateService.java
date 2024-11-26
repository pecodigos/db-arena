package com.pecodigos.dbarena.battlestate.service;

import com.pecodigos.dbarena.battlestate.model.BattleState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class BattleStateService {

    private static final String PREFIX = "battlestate:";

    private RedisTemplate<String, BattleState> redisTemplate;

    public void saveBattleState(String gameId, BattleState state) {
        String key = PREFIX + gameId;
        redisTemplate.opsForValue().set(key, state, Duration.ofMinutes(30));
    }

    public BattleState getBattleState(String gameId) {
        String key = PREFIX + gameId;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteBattleState(String gameId) {
        String key = PREFIX + gameId;
        redisTemplate.delete(key);
    }
}
