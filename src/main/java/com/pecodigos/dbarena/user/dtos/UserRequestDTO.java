package com.pecodigos.dbarena.user.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pecodigos.dbarena.user.enums.Rank;
import com.pecodigos.dbarena.user.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRequestDTO(@NotNull UUID id,
                             @NotBlank @NotNull String username,
                             @NotBlank @NotNull String email,
                             @NotBlank @NotNull String password,
                             @NotNull Integer ladderRank,
                             @NotNull String clan,
                             @NotNull Role role,
                             @NotNull Rank rank,
                             @NotNull String profilePicturePath,
                             @NotNull Integer currentLevel,
                             @NotNull Integer highestLevel,
                             @NotNull Long currentExp,
                             @NotNull Integer wins,
                             @NotNull Integer loses,
                             @NotNull Integer currentStreak,
                             @NotNull Integer highestStreak) {
}
