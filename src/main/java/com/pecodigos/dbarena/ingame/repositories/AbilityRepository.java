package com.pecodigos.dbarena.ingame.repositories;

import com.pecodigos.dbarena.ingame.entities.Ability;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AbilityRepository extends JpaRepository<Ability, Long> {
	@EntityGraph(attributePaths = {"cost"})
	List<Ability> findAllByIdIn(Collection<Long> ids);
}
