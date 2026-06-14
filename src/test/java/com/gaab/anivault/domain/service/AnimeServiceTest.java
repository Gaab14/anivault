package com.gaab.anivault.domain.service;

import com.gaab.anivault.domain.dto.AnimeRequestDto;
import com.gaab.anivault.domain.dto.AnimeResponseDto;
import com.gaab.anivault.domain.dto.AnimeUpdateDto;
import com.gaab.anivault.domain.enums.WatchStatus;
import com.gaab.anivault.domain.exception.AnimeDoesNotExistException;
import com.gaab.anivault.domain.repository.AnimeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimeServiceTest {

    @Mock
    private AnimeRepository animeRepository;

    @InjectMocks
    private AnimeService animeService;

    private AnimeResponseDto createResponse() {
        return new AnimeResponseDto(
                1L, 12345L, "Naruto", "ナルト", "image.jpg", "Synopsis",
                "Action", "Pierrot", 2002, 220, 24, 8.0, "TV",
                "Finished", WatchStatus.WATCHING, null, null, 50,
                null, null, null, null, null, null
        );
    }

    @Test
    void getAll_returnsListFromRepository() {
        Long userId = 1L;
        List<AnimeResponseDto> expected = List.of(createResponse());
        when(animeRepository.getAll(userId)).thenReturn(expected);

        List<AnimeResponseDto> result = animeService.getAll(userId);

        assertEquals(expected, result);
        verify(animeRepository).getAll(userId);
    }

    @Test
    void getById_returnsAnimeFromRepository() {
        Long userId = 1L;
        AnimeResponseDto expected = createResponse();
        when(animeRepository.getById(1L, userId)).thenReturn(expected);

        AnimeResponseDto result = animeService.getById(1L, userId);

        assertEquals(expected, result);
        verify(animeRepository).getById(1L, userId);
    }

    @Test
    void add_returnsCreatedAnime() {
        Long userId = 1L;
        AnimeRequestDto request = new AnimeRequestDto(
                12345L, "Naruto", "ナルト", "image.jpg", "Synopsis",
                "Action", "Pierrot", 2002, 220, 24, 8.0, "TV",
                "Finished", WatchStatus.WATCHING
        );
        AnimeResponseDto expected = createResponse();
        when(animeRepository.save(request, userId)).thenReturn(expected);

        AnimeResponseDto result = animeService.add(request, userId);

        assertEquals(expected, result);
        verify(animeRepository).save(request, userId);
    }

    @Test
    void update_returnsUpdatedAnime() {
        Long userId = 1L;
        AnimeUpdateDto update = new AnimeUpdateDto(
                WatchStatus.COMPLETED, null, 9, null, null, null, null, null, null
        );
        AnimeResponseDto expected = createResponse();
        when(animeRepository.update(1L, update, userId)).thenReturn(expected);

        AnimeResponseDto result = animeService.update(1L, update, userId);

        assertEquals(expected, result);
        verify(animeRepository).update(1L, update, userId);
    }

    @Test
    void delete_callsRepositoryDelete() {
        Long userId = 1L;
        doNothing().when(animeRepository).delete(1L, userId);

        animeService.delete(1L, userId);

        verify(animeRepository).delete(1L, userId);
    }

    @Test
    void getById_animeNotFound_propagatesException() {
        Long userId = 1L;
        when(animeRepository.getById(99L, userId)).thenThrow(new AnimeDoesNotExistException(99L));

        assertThrows(AnimeDoesNotExistException.class, () -> animeService.getById(99L, userId));
    }
}
