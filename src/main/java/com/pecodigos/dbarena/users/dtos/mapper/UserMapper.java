package com.pecodigos.dbarena.users.dtos.mapper;

import com.pecodigos.dbarena.users.dtos.UserRequestDTO;
import com.pecodigos.dbarena.users.dtos.UserResponseDTO;
import com.pecodigos.dbarena.users.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserRequestDTO toRequestDto(User user);
    UserResponseDTO toResponseDto(User user);
    User toEntity(UserRequestDTO userRequestDTO);
}
