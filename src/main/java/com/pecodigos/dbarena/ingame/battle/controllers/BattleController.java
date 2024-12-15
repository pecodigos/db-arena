package com.pecodigos.dbarena.ingame.battle.controllers;

import com.pecodigos.dbarena.ingame.battle.services.MatchService;
import com.pecodigos.dbarena.ingame.services.AbilityService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/battle")
@AllArgsConstructor
public class BattleController {

    private final MatchService matchService;
    private final AbilityService abilityService;
}
