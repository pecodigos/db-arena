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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
                .profilePicturePath("https://i.imgur.com/w47JVL9.png")
                .currentLevel(1)
                .highestLevel(1)
                .currentExp(0L)
                .wins(0)
                .loses(0)
                .currentStreak(0)
                .highestStreak(0)
                .build();

        return userMapper.toResponseDto(userRepository.save(user));
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

    public UserResponseDTO changePassword(UUID id, String currentPassword, String newPassword) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    public UserResponseDTO changeAvatar(UUID id, String avatarPath) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found."));

        user.setProfilePicturePath(avatarPath);
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    public PublicProfileDTO getPublicProfile(String username) {
        return userMapper.toPublicProfileDto(userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found.")));
    }
}
