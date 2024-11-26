package com.pecodigos.dbarena.battlestate.model;

import com.pecodigos.dbarena.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private LocalDateTime battleDate;

    @ManyToOne
    private User winner;
}
