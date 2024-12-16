package com.pecodigos.dbarena.ingame.repositories;

import com.pecodigos.dbarena.ingame.entities.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    Optional<Character> findByName(String name);
}
