package com.gaab.anivault.persistence.crud;

import com.gaab.anivault.persistence.entity.AnimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrudAnimeEntity extends JpaRepository<AnimeEntity, Long> {
    boolean existsByMalIdAndUserId(Long malId, Long userId);
    List<AnimeEntity> findAllByUserId(Long userId);
    Optional<AnimeEntity> findByIdAndUserId(Long id, Long userId);
}
