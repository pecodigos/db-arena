package com.pecodigos.dbarena.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

    @Length(min = 3, max = 120)
    @Column(length = 200, nullable = false)
    private String name;

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

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
