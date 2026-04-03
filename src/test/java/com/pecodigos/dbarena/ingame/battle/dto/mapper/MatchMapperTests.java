package com.pecodigos.dbarena.ingame.battle.dto.mapper;

import com.pecodigos.dbarena.ingame.battle.dto.MatchDTO;
import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.ingame.battle.models.Player;
import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.enums.Rank;
import com.pecodigos.dbarena.user.enums.Role;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class MatchMapperTests {

    private final MatchMapper matchMapper = Mappers.getMapper(MatchMapper.class);

    @Test
    void toDTOShouldMapPlayerNames() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        Player playerOne = new Player(new PublicProfileDTO(
                "goku", 1, "z-fighters", Role.MEMBER, Rank.GOOD_FIGHTER, "goku.png",
                10, 10, 1000L, 20, 5, 3, 7, createdAt
        ));
        Player playerTwo = new Player(new PublicProfileDTO(
                "vegeta", 2, "saiyans", Role.MEMBER, Rank.PRO, "vegeta.png",
                11, 11, 1200L, 22, 4, 5, 8, createdAt
        ));

        Match match = Match.builder()
                .playerOne(playerOne)
                .playerTwo(playerTwo)
                .currentPlayer(playerTwo)
                .turnNumber(3)
                .battleState(BattleState.IN_BATTLE)
                .build();

        MatchDTO dto = matchMapper.toDTO(match);

        assertEquals("goku", dto.playerOneName());
        assertEquals("vegeta", dto.playerTwoName());
        assertEquals("vegeta", dto.currentPlayerName());
        assertEquals(3, dto.turnNumber());
        assertEquals(BattleState.IN_BATTLE, dto.battleState());
    }

    @Test
    void toEntityShouldOnlyMapScalarFieldsAvailableInTheDto() {
        MatchDTO dto = new MatchDTO("goku", "vegeta", "goku", 7, BattleState.FINISHED);

        Match match = matchMapper.toEntity(dto);

        assertEquals(7, match.getTurnNumber());
        assertEquals(BattleState.FINISHED, match.getBattleState());
        assertNull(match.getPlayerOne());
        assertNull(match.getPlayerTwo());
        assertNull(match.getCurrentPlayer());
        assertFalse(match.isCurrentTurnPrepared());
    }
}
