package com.gaab.anivault.domain.dto;

import com.gaab.anivault.domain.enums.Reaction;
import com.gaab.anivault.domain.enums.WatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AnimeResponseDto(
        Long id,
        Long malId,
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

        // Info personal
        WatchStatus status,
        Reaction reaction,
        Integer userRating,
        Integer currentEpisode,
        String userReview,
        Boolean wouldRecommend,
        List<String> tags,
        LocalDate startedDate,
        LocalDate completedDate,
        LocalDateTime addedAt
) {
}
