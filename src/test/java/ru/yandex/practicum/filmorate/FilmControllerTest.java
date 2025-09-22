package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Instant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FilmControllerTest extends BaseControllerTest {

    @Test
    void createFilm_ok() throws Exception {
        Film f = new Film();
        f.setName("Inception");
        f.setDescription("Mind-bending thriller");
        f.setReleaseDate(Instant.parse("2010-07-16T00:00:00Z"));
        f.setDuration(148);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/films")
                                .contentType(json)
                                .content(toJson(f))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Inception"));
    }

    @Test
    void createFilm_emptyName_shouldFail() throws Exception {
        Film f = new Film();
        f.setName("  ");
        f.setDescription("desc");
        f.setReleaseDate(Instant.parse("2000-01-01T00:00:00Z"));
        f.setDuration(100);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(toJson(f))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_tooLongDescription_shouldFail() throws Exception {
        Film f = new Film();
        f.setName("n");
        f.setDescription("x".repeat(201)); // > 200
        f.setReleaseDate(Instant.parse("2000-01-01T00:00:00Z"));
        f.setDuration(100);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(toJson(f))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_releaseBefore1895_shouldFail() throws Exception {
        Film f = new Film();
        f.setName("n");
        f.setDescription("d");
        f.setReleaseDate(Instant.parse("1895-12-27T23:59:59Z")); // раньше 28.12.1895
        f.setDuration(100);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(toJson(f))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_nonPositiveDuration_shouldFail() throws Exception {
        Film f = new Film();
        f.setName("n");
        f.setDescription("d");
        f.setReleaseDate(Instant.parse("2000-01-01T00:00:00Z"));
        f.setDuration(0);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(toJson(f))
        ).andExpect(status().isBadRequest());
    }
}