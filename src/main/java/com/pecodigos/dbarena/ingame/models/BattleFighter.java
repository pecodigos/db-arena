package com.pecodigos.dbarena.ingame.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BattleFighter {
    private Long id;
    private String name;
    private int currentHp;
    private int currentDef;
    private static final int MAX_HP = 100;
    private boolean isStunned = false;
    private boolean isAlive = true;
}
