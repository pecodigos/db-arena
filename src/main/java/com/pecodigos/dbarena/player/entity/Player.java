package com.pecodigos.dbarena.player.entity;

import com.pecodigos.dbarena.characters.entity.Character;
import com.pecodigos.dbarena.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    @OneToOne
    private User user;

    @ManyToMany
    private List<Character> unlockedCharacters;
}
