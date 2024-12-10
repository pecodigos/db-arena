package com.pecodigos.dbarena.ingame.config;

import com.pecodigos.dbarena.ingame.services.CharacterInitService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class DataInitializer {

    private final CharacterInitService characterInitService;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            String folderPath = "src/main/resources/characters";
            characterInitService.importCharacters(folderPath);
        };
    }
}
