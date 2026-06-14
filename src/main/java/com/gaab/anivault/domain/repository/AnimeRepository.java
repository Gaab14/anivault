package com.gaab.anivault.domain.repository;

import com.gaab.anivault.domain.dto.AnimeRequestDto;
import com.gaab.anivault.domain.dto.AnimeResponseDto;
import com.gaab.anivault.domain.dto.AnimeUpdateDto;

import java.util.List;

public interface AnimeRepository {
    List<AnimeResponseDto> getAll(Long userId);
    AnimeResponseDto getById(Long id, Long userId);
    AnimeResponseDto save(AnimeRequestDto anime, Long userId);
    AnimeResponseDto update(Long id, AnimeUpdateDto anime, Long userId);
    void delete(Long id, Long userId);
}
