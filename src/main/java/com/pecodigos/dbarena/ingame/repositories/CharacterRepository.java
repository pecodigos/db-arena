package com.pecodigos.dbarena.ingame.repositories;

import com.pecodigos.dbarena.ingame.entities.Character;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    Optional<Character> findByName(String name);

    @EntityGraph(attributePaths = {"abilities"})
    Optional<Character> findWithAbilitiesByName(String name);

    @EntityGraph(attributePaths = {"abilities"})
    List<Character> findAllByIdIn(Collection<Long> ids);
}
