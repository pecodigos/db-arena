package com.pecodigos.dbarena.user.service;

import com.pecodigos.dbarena.user.dtos.mapper.UserMapper;
import com.pecodigos.dbarena.user.entity.User;
import com.pecodigos.dbarena.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UserServiceRecordMatchResultTests {

    @Test
    void recordMatchResultShouldIncrementWinnerAndLoserStats() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        UserService userService = new UserService(userMapper, userRepository, passwordEncoder);

        User winner = userWithStats("goku", 3, 1, 2, 2);
        User loser = userWithStats("vegeta", 5, 4, 3, 5);

        Mockito.when(userRepository.findByUsername("goku")).thenReturn(Optional.of(winner));
        Mockito.when(userRepository.findByUsername("vegeta")).thenReturn(Optional.of(loser));

        userService.recordMatchResult("goku", "vegeta", "goku");

        assertEquals(4, winner.getWins());
        assertEquals(3, winner.getCurrentStreak());
        assertEquals(3, winner.getHighestStreak());

        assertEquals(5, loser.getWins());
        assertEquals(5, loser.getLoses());
        assertEquals(-1, loser.getCurrentStreak());
        assertEquals(5, loser.getHighestStreak());

        verify(userRepository).save(winner);
        verify(userRepository).save(loser);
    }

    @Test
    void recordMatchResultShouldResetWinnerLosingStreakAndIgnoreInvalidWinner() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        UserService userService = new UserService(userMapper, userRepository, passwordEncoder);

        User winner = userWithStats("goku", 7, 6, -3, 4);
        User loser = userWithStats("vegeta", 2, 8, -2, 2);

        Mockito.when(userRepository.findByUsername("goku")).thenReturn(Optional.of(winner));
        Mockito.when(userRepository.findByUsername("vegeta")).thenReturn(Optional.of(loser));

        userService.recordMatchResult("goku", "vegeta", "goku");

        assertEquals(8, winner.getWins());
        assertEquals(1, winner.getCurrentStreak());
        assertEquals(4, winner.getHighestStreak());

        assertEquals(9, loser.getLoses());
        assertEquals(-3, loser.getCurrentStreak());

        userService.recordMatchResult("goku", "vegeta", "piccolo");

        verify(userRepository, never()).findByUsername("piccolo");
    }

    private static User userWithStats(String username, int wins, int loses, int currentStreak, int highestStreak) {
        return User.builder()
                .username(username)
                .wins(wins)
                .loses(loses)
                .currentStreak(currentStreak)
                .highestStreak(highestStreak)
                .build();
    }
}
