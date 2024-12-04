package com.pecodigos.dbarena.ingame.player.model;

import com.pecodigos.dbarena.ingame.characters.model.Character;
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

    @OneToMany
    private List<Character> unlockedCharacters;
}
