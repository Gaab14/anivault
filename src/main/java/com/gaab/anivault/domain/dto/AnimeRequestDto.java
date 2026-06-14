package com.gaab.anivault.domain.dto;

import com.gaab.anivault.domain.enums.WatchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnimeRequestDto(
        @NotNull
        Long malId,
        @NotBlank
        String title,
        String titleJapanese,
        String imageUrl,
        String synopsis,
        String genre,
        String studio,
        Integer year,
        Integer totalEpisodes,
        Integer episodeDuration,
        Double malScore,
        String type,
        String airedStatus,
        WatchStatus status
) {
}
