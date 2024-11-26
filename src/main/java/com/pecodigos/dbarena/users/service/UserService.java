package com.pecodigos.dbarena.users.service;

import com.pecodigos.dbarena.users.dtos.UserRequestDTO;
import com.pecodigos.dbarena.users.dtos.UserResponseDTO;
import com.pecodigos.dbarena.users.dtos.mapper.UserMapper;
import com.pecodigos.dbarena.users.entity.User;
import com.pecodigos.dbarena.exceptions.UserAlreadyExistsException;
import com.pecodigos.dbarena.exceptions.UserNotFoundException;
import com.pecodigos.dbarena.users.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private UserRepository userRepository;

    public UserResponseDTO create(UserRequestDTO userRequestDTO) {
        if (userRepository.findByUsername(userRequestDTO.username()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken.");
        }

        if (userRepository.findByEmail(userRequestDTO.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already taken.");
        }

        var user = User.builder()
                .name(userRequestDTO.name())
                .username(userRequestDTO.username())
                .email(userRequestDTO.email())
                .password(userRequestDTO.password())
                .build();

        return userMapper.toResponseDto(userRepository.save(user));
    }

    // Development -----

    public List<UserResponseDTO> list() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    public UserResponseDTO find(UUID id) {
        return userMapper.toResponseDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found.")));
    }


    public UserResponseDTO update(UUID id, UserRequestDTO userRequestDTO) {
        if (userRepository.findByUsername(userRequestDTO.username()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken.");
        }

        if (userRepository.findByEmail(userRequestDTO.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already taken.");
        }

        return userRepository.findById(id)
                .map(data -> {
                    data.setName(userRequestDTO.name());
                    data.setUsername(userRequestDTO.username());
                    data.setEmail(userRequestDTO.email());
                    data.setPassword(userRequestDTO.password());

                    return userMapper.toResponseDto(userRepository.save(data));
                }).orElseThrow(() -> new UserNotFoundException("No user found with that ID."));
    }
}
