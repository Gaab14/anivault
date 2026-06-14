package com.gaab.anivault.persistence;

import com.gaab.anivault.domain.dto.AnimeRequestDto;
import com.gaab.anivault.domain.dto.AnimeResponseDto;
import com.gaab.anivault.domain.dto.AnimeUpdateDto;
import com.gaab.anivault.domain.exception.AnimeAlreadyExistsException;
import com.gaab.anivault.domain.exception.AnimeDoesNotExistException;
import com.gaab.anivault.domain.exception.EpisodeDoesNotExistException;
import com.gaab.anivault.domain.repository.AnimeRepository;
import com.gaab.anivault.persistence.crud.CrudAnimeEntity;
import com.gaab.anivault.persistence.crud.CrudUserEntity;
import com.gaab.anivault.persistence.entity.AnimeEntity;
import com.gaab.anivault.persistence.entity.UserEntity;
import com.gaab.anivault.persistence.mapper.AnimeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AnimeEntityRepository implements AnimeRepository {
    private final CrudAnimeEntity crudAnimeEntity;
    private final CrudUserEntity crudUserEntity;
    private final AnimeMapper animeMapper;

    public AnimeEntityRepository(CrudAnimeEntity crudAnimeEntity, CrudUserEntity crudUserEntity, AnimeMapper animeMapper) {
        this.crudAnimeEntity = crudAnimeEntity;
        this.crudUserEntity = crudUserEntity;
        this.animeMapper = animeMapper;
    }

    @Override
    public List<AnimeResponseDto> getAll(Long userId) {
        return this.animeMapper.toDto(this.crudAnimeEntity.findAllByUserId(userId));
    }

    @Override
    public AnimeResponseDto getById(Long id, Long userId) {
        AnimeEntity entity = this.crudAnimeEntity.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AnimeDoesNotExistException(id));
        return this.animeMapper.toDto(entity);
    }

    @Override
    public AnimeResponseDto save(AnimeRequestDto anime, Long userId) {
        if (this.crudAnimeEntity.existsByMalIdAndUserId(anime.malId(), userId)) {
            throw new AnimeAlreadyExistsException(anime.title());
        }
        UserEntity user = this.crudUserEntity.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AnimeEntity entity = this.animeMapper.toEntity(anime);
        entity.setUser(user);
        return this.animeMapper.toDto(this.crudAnimeEntity.save(entity));
    }

    @Override
    public AnimeResponseDto update(Long id, AnimeUpdateDto anime, Long userId) {
        AnimeEntity entity = this.crudAnimeEntity.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AnimeDoesNotExistException(id));
        if (anime.currentEpisode() != null &&
                (anime.currentEpisode() < 1 || anime.currentEpisode() > entity.getTotalEpisodes())) {
            throw new EpisodeDoesNotExistException(anime.currentEpisode());
        }
        this.animeMapper.updateEntity(anime, entity);
        return this.animeMapper.toDto(this.crudAnimeEntity.save(entity));
    }

    @Override
    public void delete(Long id, Long userId) {
        AnimeEntity entity = this.crudAnimeEntity.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AnimeDoesNotExistException(id));
        this.crudAnimeEntity.delete(entity);
    }
}
