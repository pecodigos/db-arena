package com.pecodigos.dbarena.ingame.repositories;

import com.pecodigos.dbarena.ingame.entities.MatchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchLogRepository extends JpaRepository<MatchLog, Long> {
}
