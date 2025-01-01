package com.pecodigos.dbarena.ingame.battle.controllers;

import com.pecodigos.dbarena.ingame.battle.dto.MatchInfoDTO;
import com.pecodigos.dbarena.ingame.battle.dto.SearchMatchRequest;
import com.pecodigos.dbarena.ingame.battle.dto.TurnActions;
import com.pecodigos.dbarena.ingame.battle.services.MatchService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class MatchController {

    private MatchService matchService;

    @MessageMapping("/battle/search")
    @SendToUser("queue/search-status")
    public String searchForMatch(@Payload SearchMatchRequest request, Principal principal) {
        matchService.searchForMatch(principal.getName(), request.team(), request.battleQueueType());
        return "Searching for match";
    }

    @MessageMapping("/battle/get-match")
    @SendToUser("/queue/match")
    public MatchInfoDTO getMatch(Principal principal) {
        return matchService.getMatch(principal.getName());
    }

    @MessageMapping("/battle/end-turn")
    public void endTurn(@Payload TurnActions turnActions, Principal principal) {
        try {
            matchService.endTurn(principal.getName(), turnActions);
        } catch (IllegalStateException e) {
            matchService.notifyError(principal.getName(), e.getMessage());
        }
    }
}
