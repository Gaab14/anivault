package com.gaab.anivault.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaab.anivault.domain.dto.*;
import com.gaab.anivault.domain.enums.WatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnimeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() throws Exception {
        tokenA = registerAndGetToken("animeUserA" + System.nanoTime(), "animeA" + System.nanoTime() + "@email.com");
        tokenB = registerAndGetToken("animeUserB" + System.nanoTime(), "animeB" + System.nanoTime() + "@email.com");
    }

    private String registerAndGetToken(String username, String email) throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(username, email, "password123");
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        AuthResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponseDto.class);
        return response.token();
    }

    private AnimeRequestDto createAnimeRequest(Long malId, String title) {
        return new AnimeRequestDto(
                malId, title, "日本語", "image.jpg", "Synopsis",
                "Action", "Studio", 2020, 24, 24, 8.5, "TV",
                "Finished", WatchStatus.WATCHING
        );
    }

    private Long addAnimeAndGetId(String token, Long malId, String title) throws Exception {
        AnimeRequestDto request = createAnimeRequest(malId, title);
        MvcResult result = mockMvc.perform(post("/animes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        AnimeResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AnimeResponseDto.class);
        return response.id();
    }

    @Test
    void getAll_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/animes")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/animes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/animes")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void add_authenticated_returns201() throws Exception {
        AnimeRequestDto request = createAnimeRequest(10001L, "Test Anime");

        mockMvc.perform(post("/animes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Anime"))
                .andExpect(jsonPath("$.malId").value(10001));
    }

    @Test
    void getById_authenticated_ownsAnime_returns200() throws Exception {
        Long animeId = addAnimeAndGetId(tokenA, 20001L, "My Anime");

        mockMvc.perform(get("/animes/" + animeId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My Anime"));
    }

    @Test
    void getById_authenticated_doesNotOwnAnime_returns404() throws Exception {
        Long animeId = addAnimeAndGetId(tokenA, 20002L, "User A Anime");

        mockMvc.perform(get("/animes/" + animeId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_authenticated_ownsAnime_returns200() throws Exception {
        Long animeId = addAnimeAndGetId(tokenA, 30001L, "Update Test");
        AnimeUpdateDto update = new AnimeUpdateDto(
                WatchStatus.COMPLETED, null, 9, 24, "Great!", true, null, null, null
        );

        mockMvc.perform(put("/animes/" + animeId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.userRating").value(9));
    }

    @Test
    void delete_authenticated_ownsAnime_returns204() throws Exception {
        Long animeId = addAnimeAndGetId(tokenA, 40001L, "Delete Test");

        mockMvc.perform(delete("/animes/" + animeId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe
        mockMvc.perform(get("/animes/" + animeId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    void userIsolation_userACannotSeeUserBAnimes() throws Exception {
        addAnimeAndGetId(tokenA, 50001L, "User A Only");
        addAnimeAndGetId(tokenB, 50002L, "User B Only");

        // User A solo ve sus animes
        mockMvc.perform(get("/animes")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("User A Only"));

        // User B solo ve sus animes
        mockMvc.perform(get("/animes")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("User B Only"));
    }

    @Test
    void add_duplicateMalIdSameUser_returns409() throws Exception {
        AnimeRequestDto request = createAnimeRequest(60001L, "Duplicate Test");

        mockMvc.perform(post("/animes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/animes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void add_duplicateMalIdDifferentUser_returns201() throws Exception {
        AnimeRequestDto request = createAnimeRequest(70001L, "Same Anime");

        mockMvc.perform(post("/animes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/animes")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
