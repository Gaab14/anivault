package com.gaab.anivault.persistence;

import com.gaab.anivault.domain.dto.AnimeRequestDto;
import com.gaab.anivault.domain.dto.AnimeResponseDto;
import com.gaab.anivault.domain.dto.AnimeUpdateDto;
import com.gaab.anivault.domain.enums.WatchStatus;
import com.gaab.anivault.domain.exception.AnimeAlreadyExistsException;
import com.gaab.anivault.domain.exception.AnimeDoesNotExistException;
import com.gaab.anivault.domain.exception.EpisodeDoesNotExistException;
import com.gaab.anivault.persistence.crud.CrudAnimeEntity;
import com.gaab.anivault.persistence.crud.CrudUserEntity;
import com.gaab.anivault.persistence.entity.AnimeEntity;
import com.gaab.anivault.persistence.entity.UserEntity;
import com.gaab.anivault.persistence.mapper.AnimeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimeEntityRepositoryTest {

    @Mock
    private CrudAnimeEntity crudAnimeEntity;
    @Mock
    private CrudUserEntity crudUserEntity;
    @Mock
    private AnimeMapper animeMapper;

    @InjectMocks
    private AnimeEntityRepository animeEntityRepository;

    private AnimeEntity createEntity() {
        return AnimeEntity.builder()
                .id(1L)
                .malId(12345L)
                .title("Naruto")
                .totalEpisodes(220)
                .build();
    }

    private AnimeResponseDto createResponseDto() {
        return new AnimeResponseDto(
                1L, 12345L, "Naruto", null, null, null,
                null, null, null, 220, null, null, null,
                null, WatchStatus.WATCHING, null, null, null,
                null, null, null, null, null, null
        );
    }

    @Test
    void getAll_returnsUserSpecificAnimes() {
        Long userId = 1L;
        List<AnimeEntity> entities = List.of(createEntity());
        List<AnimeResponseDto> dtos = List.of(createResponseDto());

        when(crudAnimeEntity.findAllByUserId(userId)).thenReturn(entities);
        when(animeMapper.toDto(entities)).thenReturn(dtos);

        List<AnimeResponseDto> result = animeEntityRepository.getAll(userId);

        assertEquals(1, result.size());
        verify(crudAnimeEntity).findAllByUserId(userId);
        verify(crudAnimeEntity, never()).findAll();
    }

    @Test
    void getById_userOwnsAnime_returnsDto() {
        Long userId = 1L;
        AnimeEntity entity = createEntity();
        AnimeResponseDto dto = createResponseDto();

        when(crudAnimeEntity.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(entity));
        when(animeMapper.toDto(entity)).thenReturn(dto);

        AnimeResponseDto result = animeEntityRepository.getById(1L, userId);

        assertNotNull(result);
        assertEquals("Naruto", result.title());
    }

    @Test
    void getById_userDoesNotOwnAnime_throwsException() {
        Long userId = 2L;
        when(crudAnimeEntity.findByIdAndUserId(1L, userId)).thenReturn(Optional.empty());

        assertThrows(AnimeDoesNotExistException.class,
                () -> animeEntityRepository.getById(1L, userId));
    }

    @Test
    void save_newAnime_savesWithUserRelationship() {
        Long userId = 1L;
        AnimeRequestDto request = new AnimeRequestDto(
                12345L, "Naruto", null, null, null, null,
                null, null, null, null, null, null, null, WatchStatus.WATCHING
        );
        AnimeEntity entity = createEntity();
        UserEntity user = UserEntity.builder().id(userId).username("testuser").build();
        AnimeResponseDto dto = createResponseDto();

        when(crudAnimeEntity.existsByMalIdAndUserId(12345L, userId)).thenReturn(false);
        when(crudUserEntity.findById(userId)).thenReturn(Optional.of(user));
        when(animeMapper.toEntity(request)).thenReturn(entity);
        when(crudAnimeEntity.save(entity)).thenReturn(entity);
        when(animeMapper.toDto(entity)).thenReturn(dto);

        AnimeResponseDto result = animeEntityRepository.save(request, userId);

        assertNotNull(result);
        verify(crudAnimeEntity).save(entity);
        assertEquals(user, entity.getUser());
    }

    @Test
    void save_duplicateMalIdForSameUser_throwsAlreadyExists() {
        Long userId = 1L;
        AnimeRequestDto request = new AnimeRequestDto(
                12345L, "Naruto", null, null, null, null,
                null, null, null, null, null, null, null, WatchStatus.WATCHING
        );

        when(crudAnimeEntity.existsByMalIdAndUserId(12345L, userId)).thenReturn(true);

        assertThrows(AnimeAlreadyExistsException.class,
                () -> animeEntityRepository.save(request, userId));
        verify(crudAnimeEntity, never()).save(any());
    }

    @Test
    void save_sameMalIdDifferentUser_allowsSave() {
        Long userId = 2L;
        AnimeRequestDto request = new AnimeRequestDto(
                12345L, "Naruto", null, null, null, null,
                null, null, null, null, null, null, null, WatchStatus.WATCHING
        );
        AnimeEntity entity = createEntity();
        UserEntity user = UserEntity.builder().id(userId).username("otheruser").build();
        AnimeResponseDto dto = createResponseDto();

        when(crudAnimeEntity.existsByMalIdAndUserId(12345L, userId)).thenReturn(false);
        when(crudUserEntity.findById(userId)).thenReturn(Optional.of(user));
        when(animeMapper.toEntity(request)).thenReturn(entity);
        when(crudAnimeEntity.save(entity)).thenReturn(entity);
        when(animeMapper.toDto(entity)).thenReturn(dto);

        AnimeResponseDto result = animeEntityRepository.save(request, userId);

        assertNotNull(result);
    }

    @Test
    void update_validEpisode_updatesSuccessfully() {
        Long userId = 1L;
        AnimeEntity entity = createEntity();
        AnimeUpdateDto update = new AnimeUpdateDto(
                WatchStatus.WATCHING, null, null, 100, null, null, null, null, null
        );
        AnimeResponseDto dto = createResponseDto();

        when(crudAnimeEntity.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(entity));
        when(crudAnimeEntity.save(entity)).thenReturn(entity);
        when(animeMapper.toDto(entity)).thenReturn(dto);

        AnimeResponseDto result = animeEntityRepository.update(1L, update, userId);

        assertNotNull(result);
        verify(animeMapper).updateEntity(update, entity);
    }

    @Test
    void update_invalidEpisode_throwsException() {
        Long userId = 1L;
        AnimeEntity entity = createEntity(); // totalEpisodes = 220
        AnimeUpdateDto update = new AnimeUpdateDto(
                null, null, null, 999, null, null, null, null, null
        );

        when(crudAnimeEntity.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(entity));

        assertThrows(EpisodeDoesNotExistException.class,
                () -> animeEntityRepository.update(1L, update, userId));
    }

    @Test
    void delete_userOwnsAnime_deletes() {
        Long userId = 1L;
        AnimeEntity entity = createEntity();

        when(crudAnimeEntity.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(entity));

        animeEntityRepository.delete(1L, userId);

        verify(crudAnimeEntity).delete(entity);
    }

    @Test
    void delete_userDoesNotOwnAnime_throwsException() {
        Long userId = 2L;
        when(crudAnimeEntity.findByIdAndUserId(1L, userId)).thenReturn(Optional.empty());

        assertThrows(AnimeDoesNotExistException.class,
                () -> animeEntityRepository.delete(1L, userId));
    }
}
