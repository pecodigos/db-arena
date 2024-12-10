package com.pecodigos.dbarena.ingame.controllers;


import com.pecodigos.dbarena.ingame.dtos.CharacterDTO;
import com.pecodigos.dbarena.ingame.services.CharacterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
@AllArgsConstructor
public class CharacterController {

    private CharacterService characterService;

    @GetMapping("/")
    public ResponseEntity<List<CharacterDTO>> getAllCharacters() {
        return ResponseEntity.ok(characterService.getAllCharacters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharacterDTO> getCharacterByName(@PathVariable Long id) {
        return ResponseEntity.ok(characterService.getCharacter(id));
    }
}
