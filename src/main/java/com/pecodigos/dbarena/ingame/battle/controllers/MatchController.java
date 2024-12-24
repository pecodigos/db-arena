package com.pecodigos.dbarena.ingame.battle.controllers;

import com.pecodigos.dbarena.ingame.battle.dto.MatchInfoDTO;
import com.pecodigos.dbarena.ingame.battle.models.Match;
import com.pecodigos.dbarena.ingame.battle.services.MatchService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class MatchController {

    private MatchService matchService;

    @MessageMapping("/battle/search")
    @SendToUser("queue/search-status")
    public String searchForMatch(Principal principal) {
        System.out.println("Received search for match request from: " + principal.getName());
        matchService.searchForMatch(principal.getName());
        return "Searching for match";
    }

    @MessageMapping("/battle/get-match")
    @SendToUser("/queue/match")
    public MatchInfoDTO getMatch(Principal principal) {
        System.out.println("Received get match request from: " + principal.getName());
        return matchService.getMatch(principal.getName());
    }

    @MessageMapping("/battle/end-turn")
    public void endTurn(Principal principal) {
        System.out.println("Received endTurn request from: " + principal.getName());
        matchService.endTurn(principal.getName());
    }
}
