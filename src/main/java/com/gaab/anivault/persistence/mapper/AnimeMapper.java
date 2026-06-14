package com.gaab.anivault.persistence.mapper;

import com.gaab.anivault.domain.dto.AnimeRequestDto;
import com.gaab.anivault.domain.dto.AnimeResponseDto;
import com.gaab.anivault.domain.dto.AnimeUpdateDto;
import com.gaab.anivault.persistence.entity.AnimeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnimeMapper {
    AnimeResponseDto toDto(AnimeEntity animeEntity);
    List<AnimeResponseDto> toDto(List<AnimeEntity> animeEntity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    @Mapping(target = "reaction", ignore = true)
    @Mapping(target = "userRating", ignore = true)
    @Mapping(target = "currentEpisode", ignore = true)
    @Mapping(target = "userReview", ignore = true)
    @Mapping(target = "wouldRecommend", ignore = true)
    @Mapping(target = "completedDate", ignore = true)
    @Mapping(target = "startedDate", ignore = true)
    @Mapping(target = "tags", ignore = true)
    AnimeEntity toEntity(AnimeRequestDto dto);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "titleJapanese", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "synopsis", ignore = true)
    @Mapping(target = "genre", ignore = true)
    @Mapping(target = "studio", ignore = true)
    @Mapping(target = "episodeDuration", ignore = true)
    @Mapping(target = "totalEpisodes", ignore = true)
    @Mapping(target = "airedStatus", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "malId", ignore = true)
    @Mapping(target = "malScore", ignore = true)
    void updateEntity(AnimeUpdateDto dto, @MappingTarget AnimeEntity entity);
}
