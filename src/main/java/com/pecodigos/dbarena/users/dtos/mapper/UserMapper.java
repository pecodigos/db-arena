package com.pecodigos.dbarena.users.dtos.mapper;

import com.pecodigos.dbarena.users.dtos.UserResponseDTO;
import com.pecodigos.dbarena.users.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserResponseDTO toResponseDto(User user);
}
