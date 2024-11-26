package com.pecodigos.dbarena.config;

import com.pecodigos.dbarena.characters.dtos.mapper.CharacterMapper;
import com.pecodigos.dbarena.users.dtos.mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public UserMapper userMapper() {
        return Mappers.getMapper(UserMapper.class);
    }

    @Bean
    public CharacterMapper characterMapper() {
        return Mappers.getMapper(CharacterMapper.class);
    }
}
