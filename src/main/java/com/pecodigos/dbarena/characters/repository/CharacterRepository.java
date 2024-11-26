package com.pecodigos.dbarena.characters.repository;

import com.pecodigos.dbarena.characters.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterRepository extends JpaRepository<Character, Long> {
}
