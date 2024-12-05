package com.pecodigos.dbarena.ingame.repositories;

import com.pecodigos.dbarena.ingame.models.BattleRoom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BattleRoomRepository extends CrudRepository<BattleRoom, UUID> {
}
