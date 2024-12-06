package com.pecodigos.dbarena.user.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pecodigos.dbarena.user.enums.Rank;
import com.pecodigos.dbarena.user.enums.Role;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PublicProfileDTO(@NotNull String username,
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
                               @NotNull Integer highestStreak,
                               @NotNull @JsonFormat(pattern = "MMMM dd, yyyy") LocalDateTime createdAt) {
}
