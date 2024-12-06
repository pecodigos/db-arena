package com.pecodigos.dbarena.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pecodigos.dbarena.user.enums.Rank;
import com.pecodigos.dbarena.user.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Length(min = 3, max = 21)
    @Column(length = 21, nullable = false)
    private String username;

    @Email
    @Length(min = 10, max = 120)
    @Column(length = 120, nullable = false)
    private String email;

    @Length(min = 9, max = 120)
    @Column(length = 120, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Rank rank;

    private String profilePicturePath;
    private Integer currentLevel;
    private Integer highestLevel;
    private Long currentExp;
    private Integer wins;
    private Integer loses;
    private Integer currentStreak;
    private Integer highestStreak;

    @CreationTimestamp
    @JsonFormat(pattern = "MMMM dd, yyyy")
    private LocalDateTime createdAt;
}
