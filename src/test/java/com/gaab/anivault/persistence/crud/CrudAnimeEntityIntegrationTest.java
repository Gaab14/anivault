package com.gaab.anivault.persistence.crud;

import com.gaab.anivault.domain.enums.Role;
import com.gaab.anivault.domain.enums.WatchStatus;
import com.gaab.anivault.persistence.entity.AnimeEntity;
import com.gaab.anivault.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CrudAnimeEntityIntegrationTest {

    @Autowired
    private CrudAnimeEntity crudAnimeEntity;

    @Autowired
    private CrudUserEntity crudUserEntity;

    private UserEntity userA;
    private UserEntity userB;

    @BeforeEach
    void setUp() {
        userA = crudUserEntity.save(UserEntity.builder()
                .username("userA").email("a@email.com")
                .password("pass").role(Role.USER).build());
        userB = crudUserEntity.save(UserEntity.builder()
                .username("userB").email("b@email.com")
                .password("pass").role(Role.USER).build());
    }

    private AnimeEntity createAnime(Long malId, String title, UserEntity user) {
        return AnimeEntity.builder()
                .malId(malId)
                .title(title)
                .totalEpisodes(24)
                .status(WatchStatus.WATCHING)
                .user(user)
                .build();
    }

    @Test
    void findAllByUserId_returnsOnlyUserAnimes() {
        crudAnimeEntity.save(createAnime(1L, "Naruto", userA));
        crudAnimeEntity.save(createAnime(2L, "Bleach", userA));
        crudAnimeEntity.save(createAnime(3L, "One Piece", userB));

        List<AnimeEntity> userAAnimes = crudAnimeEntity.findAllByUserId(userA.getId());
        List<AnimeEntity> userBAnimes = crudAnimeEntity.findAllByUserId(userB.getId());

        assertEquals(2, userAAnimes.size());
        assertEquals(1, userBAnimes.size());
        assertEquals("One Piece", userBAnimes.get(0).getTitle());
    }

    @Test
    void findByIdAndUserId_existsForUser_returnsAnime() {
        AnimeEntity saved = crudAnimeEntity.save(createAnime(1L, "Naruto", userA));

        Optional<AnimeEntity> found = crudAnimeEntity.findByIdAndUserId(saved.getId(), userA.getId());

        assertTrue(found.isPresent());
        assertEquals("Naruto", found.get().getTitle());
    }

    @Test
    void findByIdAndUserId_existsButWrongUser_returnsEmpty() {
        AnimeEntity saved = crudAnimeEntity.save(createAnime(1L, "Naruto", userA));

        Optional<AnimeEntity> found = crudAnimeEntity.findByIdAndUserId(saved.getId(), userB.getId());

        assertTrue(found.isEmpty());
    }

    @Test
    void existsByMalIdAndUserId_trueWhenExists() {
        crudAnimeEntity.save(createAnime(12345L, "Naruto", userA));

        assertTrue(crudAnimeEntity.existsByMalIdAndUserId(12345L, userA.getId()));
    }

    @Test
    void existsByMalIdAndUserId_falseForDifferentUser() {
        crudAnimeEntity.save(createAnime(12345L, "Naruto", userA));

        assertFalse(crudAnimeEntity.existsByMalIdAndUserId(12345L, userB.getId()));
    }

    @Test
    void bothUsersCanHaveSameMalId() {
        crudAnimeEntity.save(createAnime(12345L, "Naruto", userA));
        crudAnimeEntity.save(createAnime(12345L, "Naruto", userB));

        assertTrue(crudAnimeEntity.existsByMalIdAndUserId(12345L, userA.getId()));
        assertTrue(crudAnimeEntity.existsByMalIdAndUserId(12345L, userB.getId()));

        List<AnimeEntity> allAnimes = crudAnimeEntity.findAll();
        assertEquals(2, allAnimes.size());
    }
}
