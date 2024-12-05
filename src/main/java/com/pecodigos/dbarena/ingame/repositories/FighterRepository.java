package com.pecodigos.dbarena.ingame.repositories;

import com.pecodigos.dbarena.ingame.entities.Fighter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FighterRepository extends JpaRepository<Fighter, Long> {
}
