package com.pecodigos.dbarena.ingame.battle.controllers;

import com.pecodigos.dbarena.ingame.battle.dto.MatchInfoDTO;
import com.pecodigos.dbarena.ingame.battle.dto.SearchMatchRequest;
import com.pecodigos.dbarena.ingame.battle.dto.TurnActions;
import com.pecodigos.dbarena.ingame.battle.services.MatchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@AllArgsConstructor
@Slf4j
public class MatchController {

    private MatchService matchService;

    @MessageMapping("/battle/search")
    @SendToUser("/queue/search-status")
    public String searchForMatch(@Payload SearchMatchRequest request, Principal principal) {
        if (principal == null) {
            return "Authentication required.";
        }

        try {
            matchService.searchForMatch(principal.getName(), request.characterIds(), request.battleQueueType());
            return "Searching for match";
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Match search rejected for user {}: {}", principal.getName(), e.getMessage());
            return e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error while searching match for user {}", principal.getName(), e);
            matchService.notifyError(principal.getName(), "Unable to start matchmaking. Please try again.");
            return "Unable to search match right now. Please try again.";
        }
    }

    @MessageMapping("/battle/cancel-search")
    @SendToUser("/queue/search-status")
    public String cancelSearch(Principal principal) {
        if (principal == null) {
            return "Authentication required.";
        }

        boolean cancelled = matchService.cancelSearch(principal.getName());
        if (cancelled) {
            return "Search cancelled.";
        }
        return "You were not in queue.";
    }

    @MessageMapping("/battle/get-match")
    @SendToUser("/queue/match")
    public MatchInfoDTO getMatch(Principal principal) {
        if (principal == null) {
            return null;
        }

        return matchService.getMatch(principal.getName());
    }

    @MessageMapping("/battle/end-turn")
    public void endTurn(@Payload TurnActions turnActions, Principal principal) {
        if (principal == null) {
            return;
        }

        try {
            matchService.endTurn(principal.getName(), turnActions);
        } catch (IllegalStateException e) {
            matchService.notifyError(principal.getName(), e.getMessage());
        }
    }

    @MessageMapping("/battle/forfeit")
    public void forfeit(Principal principal) {
        if (principal == null) {
            return;
        }

        try {
            matchService.forfeitMatch(principal.getName());
        } catch (IllegalStateException e) {
            matchService.notifyError(principal.getName(), e.getMessage());
        }
    }
}
