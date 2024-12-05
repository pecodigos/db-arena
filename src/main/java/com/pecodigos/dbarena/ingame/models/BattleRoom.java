package com.pecodigos.dbarena.ingame.models;

import com.pecodigos.dbarena.ingame.enums.battle.BattleState;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BattleRoom {

    private UUID id;
    private BattlePlayer playerOne;
    private BattlePlayer playerTwo;
    private boolean isPlayerOneTurn;

    @Enumerated(EnumType.STRING)
    private BattleState battleState;

    public BattlePlayer getActivePlayer() {
        return isPlayerOneTurn ? playerOne : playerTwo;
    }
}
