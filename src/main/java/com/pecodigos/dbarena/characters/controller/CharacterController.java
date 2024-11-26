package com.pecodigos.dbarena.characters.controller;

import com.pecodigos.dbarena.characters.dtos.CharacterResponseDTO;
import com.pecodigos.dbarena.characters.service.CharacterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/characters")
public class CharacterController {

    private CharacterService characterService;

    @GetMapping("/{id}")
    public ResponseEntity<CharacterResponseDTO> getCharacter(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(characterService.getCharacterById(id));
    }

    @GetMapping("/")
    public ResponseEntity<List<CharacterResponseDTO>> getAllCharacters() {
        return ResponseEntity.ok(characterService.list());
    }
}
