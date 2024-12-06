package com.pecodigos.dbarena.user.dtos.mapper;

import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import com.pecodigos.dbarena.user.dtos.UserResponseDTO;
import com.pecodigos.dbarena.user.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserResponseDTO toResponseDto(User user);
    PublicProfileDTO toPublicProfileDto(User user);
}
