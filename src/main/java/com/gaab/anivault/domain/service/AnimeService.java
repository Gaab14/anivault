package com.gaab.anivault.domain.service;

import com.gaab.anivault.domain.dto.AnimeRequestDto;
import com.gaab.anivault.domain.dto.AnimeResponseDto;
import com.gaab.anivault.domain.dto.AnimeUpdateDto;
import com.gaab.anivault.domain.repository.AnimeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimeService {
    private final AnimeRepository animeRepository;

    public AnimeService(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    public List<AnimeResponseDto> getAll(Long userId) {
        return animeRepository.getAll(userId);
    }

    public AnimeResponseDto getById(Long id, Long userId) {
        return animeRepository.getById(id, userId);
    }

    public AnimeResponseDto add(AnimeRequestDto anime, Long userId) {
        return animeRepository.save(anime, userId);
    }

    public AnimeResponseDto update(Long id, AnimeUpdateDto anime, Long userId) {
        return animeRepository.update(id, anime, userId);
    }

    public void delete(Long id, Long userId) {
        animeRepository.delete(id, userId);
    }
}
