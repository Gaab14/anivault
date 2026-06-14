package com.gaab.anivault.persistence.entity;

import com.gaab.anivault.domain.enums.Reaction;
import com.gaab.anivault.domain.enums.WatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String titleJapanese;
    private String imageUrl;
    @Column(columnDefinition = "TEXT")
    private String synopsis;
    private String genre;
    private String studio;
    private Integer episodeDuration;
    private Integer totalEpisodes;
    private LocalDate startedDate;
    private String airedStatus;
    @Column(name = "anime_type")
    private String type;
    @Column(name = "release_year")
    private Integer year;
    private Long malId;
    @Enumerated(EnumType.STRING)
    private WatchStatus status;
    private Double malScore;
    @Enumerated(EnumType.STRING)
    private Reaction reaction;
    private Integer userRating;
    private Integer currentEpisode;
    @Column(columnDefinition = "TEXT")
    private String userReview;
    private Boolean wouldRecommend;
    private LocalDate completedDate;
    private LocalDateTime addedAt;
    @ElementCollection
    private List<String> tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
    }
}
