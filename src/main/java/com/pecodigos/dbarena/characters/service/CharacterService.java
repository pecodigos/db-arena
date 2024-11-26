package com.pecodigos.dbarena.characters.service;

import com.pecodigos.dbarena.characters.dtos.mapper.CharacterMapper;
import com.pecodigos.dbarena.characters.repository.CharacterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CharacterService {

    private CharacterRepository characterRepository;
    private CharacterMapper characterMapper;
}
