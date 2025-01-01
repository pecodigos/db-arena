package com.pecodigos.dbarena.ingame.entities;

import com.pecodigos.dbarena.ingame.enums.battle.BattleQueueType;
import com.pecodigos.dbarena.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "battle_logs")
public class MatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String playerOneUsername;

    @Column(nullable = false)
    private String playerTwoUsername;

    private String winnerUsername;

    @Enumerated(EnumType.STRING)
    private BattleQueueType battleQueueType;

    @CreationTimestamp
    private LocalDateTime battleDate;
}
