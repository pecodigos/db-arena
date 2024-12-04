package com.pecodigos.dbarena.ingame.battlestate.model;

import com.pecodigos.dbarena.ingame.enums.BattleQueueType;
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
public class BattleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_one_id", nullable = false)
    private User playerOne;

    @ManyToOne
    @JoinColumn(name = "player_two_id", nullable = false)
    private User playerTwo;

    @ManyToOne
    private User winner;

    @Enumerated(EnumType.STRING)
    private BattleQueueType battleQueueType;

    @CreationTimestamp
    private LocalDateTime battleDate;
}
