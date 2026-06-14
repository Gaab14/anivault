package com.gaab.anivault.domain.dto;

import com.gaab.anivault.domain.enums.Reaction;
import com.gaab.anivault.domain.enums.WatchStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;

public record AnimeUpdateDto(
        WatchStatus status,
        Reaction reaction,
        @Min(value = 1, message = "El rating no puede ser menor que 1")
        @Max(value = 10, message = "El rating no puede ser mayor que 10")
        Integer userRating,
        Integer currentEpisode,
        String userReview,
        Boolean wouldRecommend,
        List<String> tags,
        LocalDate startedDate,
        @PastOrPresent(message = "La fecha no puede ser futura")
        LocalDate completedDate
) {
}
