package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FilmControllerTest extends BaseControllerTest {

    @Test
    void createFilm_ok() throws Exception {
        Film f = new Film();
        f.setName("Inception");
        f.setDescription("Mind-bending thriller");
        f.setReleaseDate(LocalDate.of(2010, 7, 16));
        f.setDuration(148);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/films")
                                .contentType(json)
                                .content(toJson(f))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Inception"));
    }

    @Test
    void createFilm_emptyName_shouldFail() throws Exception {
        Film f = new Film();
        f.setName("  ");
        f.setDescription("desc");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
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
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
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
        f.setReleaseDate(LocalDate.of(1895, 12, 27)); // раньше 28.12.1895
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
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(0);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(toJson(f))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void addLike_ok() throws Exception {
        long filmId = createFilmAndGetId(
                "Blade Runner", "desc", LocalDate.of(2000, 1, 1), 100);
        long userId = createUserAndGetId(
                "deckard@example.com", "deckard", "Rick Deckard", LocalDate.of(1990, 1, 1));

        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }

    @Test
    void addLike_idempotent() throws Exception {
        long filmId = createFilmAndGetId(
                "Interstellar", "desc", LocalDate.of(2000, 1, 1), 100);
        long userId = createUserAndGetId(
                "coop@example.com", "coop", "Cooper", LocalDate.of(1990, 1, 1));

        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        // Повторный PUT — не должен падать
        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }

    @Test
    void removeLike_ok() throws Exception {
        long filmId = createFilmAndGetId(
                "Inception", "desc", LocalDate.of(2000, 1, 1), 100);
        long userId = createUserAndGetId(
                "cobb@example.com", "cobb", "Cobb", LocalDate.of(1990, 1, 1));

        // Ставим лайк
        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        // Снимаем лайк
        mockMvc.perform(MockMvcRequestBuilders.delete("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        // Повторный DELETE — идемпотентен
        mockMvc.perform(MockMvcRequestBuilders.delete("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }

    @Test
    void getTopFilms_ok_sortedAndLimited() throws Exception {
        // Фильмы
        long f1 = createFilmAndGetId("A", "d", LocalDate.of(2000, 1, 1), 100);
        long f2 = createFilmAndGetId("B", "d", LocalDate.of(2000, 1, 1), 100);
        long f3 = createFilmAndGetId("C", "d", LocalDate.of(2000, 1, 1), 100);

        // Пользователи
        long u1 = createUserAndGetId("u1@example.com", "u1", "U1", LocalDate.of(1990, 1, 1));
        long u2 = createUserAndGetId("u2@example.com", "u2", "U2", LocalDate.of(1990, 1, 1));

        // Лайки: A — 2 лайка; B — 1 лайк; C — 0
        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", f1, u1))
                .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", f1, u2))
                .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", f2, u1))
                .andExpect(status().isOk());

        // Запрашиваем топ-2: ожидаем порядок A, B
        mockMvc.perform(MockMvcRequestBuilders.get("/films/popular").param("count", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    @Test
    void getTopFilms_countNonPositive_shouldFail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/films/popular").param("count", "0"))
                .andExpect(status().isBadRequest());
    }
}
