package com.pecodigos.dbarena.user.service;

import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.dtos.UserRequestDTO;
import com.pecodigos.dbarena.user.dtos.UserResponseDTO;
import com.pecodigos.dbarena.user.dtos.mapper.UserMapper;
import com.pecodigos.dbarena.user.entity.User;
import com.pecodigos.dbarena.exceptions.UserAlreadyExistsException;
import com.pecodigos.dbarena.exceptions.UserNotFoundException;
import com.pecodigos.dbarena.user.enums.Rank;
import com.pecodigos.dbarena.user.enums.Role;
import com.pecodigos.dbarena.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private UserMapper userMapper;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    public UserResponseDTO create(UserRequestDTO userRequestDTO) {
        if (userRepository.findByUsername(userRequestDTO.username()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken.");
        }

        if (userRepository.findByEmail(userRequestDTO.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already taken.");
        }

        var user = User.builder()
                .username(userRequestDTO.username())
                .email(userRequestDTO.email())
                .password(passwordEncoder.encode(userRequestDTO.password()))
                .clan("")
                .ladderRank(0)
                .role(Role.MEMBER)
                .rank(Rank.BABY)
                .profilePicturePath("https://i.imgur.com/dB81Zwr.jpeg")
                .currentLevel(1)
                .highestLevel(1)
                .currentExp(0L)
                .wins(0)
                .loses(0)
                .currentStreak(0)
                .highestStreak(0)
                .build();

        try {
            return userMapper.toResponseDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException("Username or email already taken.");
        }
    }

    public UserResponseDTO login(UserRequestDTO userRequestDTO) {
        var optionalUser = userRepository.findByUsername(userRequestDTO.username());

        if (optionalUser.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password.");
        }

        var user = optionalUser.get();

        if (!passwordEncoder.matches(userRequestDTO.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password.");
        }

        return userMapper.toResponseDto(user);
    }

    public UserResponseDTO changePassword(UUID id, String requesterUsername, String currentPassword, String newPassword) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found."));

        if (!user.getUsername().equals(requesterUsername)) {
            throw new AccessDeniedException("You cannot change another user's password.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    public UserResponseDTO changeAvatar(UUID id, String requesterUsername, String avatarPath) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found."));

        if (!user.getUsername().equals(requesterUsername)) {
            throw new AccessDeniedException("You cannot change another user's avatar.");
        }

        user.setProfilePicturePath(avatarPath);
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    public PublicProfileDTO getPublicProfile(String username) {
        return userMapper.toPublicProfileDto(userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found.")));
    }

    @Transactional
    public void recordMatchResult(String playerOneUsername, String playerTwoUsername, String winnerUsername) {
        if (playerOneUsername == null || playerTwoUsername == null || winnerUsername == null || winnerUsername.isBlank()) {
            return;
        }

        String normalizedWinner = winnerUsername.trim();
        if (!normalizedWinner.equals(playerOneUsername) && !normalizedWinner.equals(playerTwoUsername)) {
            return;
        }

        String loserUsername = normalizedWinner.equals(playerOneUsername) ? playerTwoUsername : playerOneUsername;
        if (normalizedWinner.equals(loserUsername)) {
            return;
        }

        User winner = userRepository.findByUsername(normalizedWinner)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        User loser = userRepository.findByUsername(loserUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        winner.setWins(valueOrZero(winner.getWins()) + 1);
        int winnerStreak = valueOrZero(winner.getCurrentStreak());
        winnerStreak = winnerStreak >= 0 ? winnerStreak + 1 : 1;
        winner.setCurrentStreak(winnerStreak);
        winner.setHighestStreak(Math.max(valueOrZero(winner.getHighestStreak()), winnerStreak));

        loser.setLoses(valueOrZero(loser.getLoses()) + 1);
        int loserStreak = valueOrZero(loser.getCurrentStreak());
        loserStreak = loserStreak <= 0 ? loserStreak - 1 : -1;
        loser.setCurrentStreak(loserStreak);
        loser.setHighestStreak(valueOrZero(loser.getHighestStreak()));

        userRepository.save(winner);
        userRepository.save(loser);
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }
}
