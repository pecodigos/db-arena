//package com.pecodigos.dbarena.ingame.battle.controllers;
//
//import com.pecodigos.dbarena.ingame.battle.services.MatchService;
//import lombok.AllArgsConstructor;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/battle")
//@AllArgsConstructor
//public class MatchController {
//
//    private MatchService matchService;
//    private SimpMessagingTemplate messagingTemplate;
//
//    @PostMapping("/search")
//    public void searchForMatch(@RequestParam String playerId) {
//        matchService.searchForMatch(playerId);
//    }
//}
