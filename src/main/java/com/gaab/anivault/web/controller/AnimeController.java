package com.gaab.anivault.web.controller;

import com.gaab.anivault.domain.dto.AnimeRequestDto;
import com.gaab.anivault.domain.dto.AnimeResponseDto;
import com.gaab.anivault.domain.dto.AnimeUpdateDto;
import com.gaab.anivault.domain.service.AnimeService;
import com.gaab.anivault.web.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/animes")
public class AnimeController {
    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @GetMapping
    public ResponseEntity<List<AnimeResponseDto>> getAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(animeService.getAll(userDetails.getUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimeResponseDto> getById(@PathVariable Long id,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(animeService.getById(id, userDetails.getUserId()));
    }

    @PostMapping
    public ResponseEntity<AnimeResponseDto> add(@RequestBody @Valid AnimeRequestDto anime,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.animeService.add(anime, userDetails.getUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimeResponseDto> update(@PathVariable Long id,
                                                   @RequestBody @Valid AnimeUpdateDto anime,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(this.animeService.update(id, anime, userDetails.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        this.animeService.delete(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
