package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FilmControllerTest extends BaseControllerTest {

    //Positive

    @Test
    void createFilm_ok() throws Exception {
        long id = createFilmAndGetId(
                "Inception", "d", LocalDate.of(2010, 7, 16), 148,
                "PG-13", new LinkedHashSet<>(Set.of(1L, 4L))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/films/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Inception"))
                .andExpect(jsonPath("$.rating").value("PG-13"))
                .andExpect(jsonPath("$.genres.length()").value(2))
                .andExpect(jsonPath("$.genres[0]").value(1))
                .andExpect(jsonPath("$.genres[1]").value(4));
    }

    //Validation: create

    @Test
    void createFilm_invalidRating_shouldReturn404() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "n");
        body.put("description", "d");
        body.put("releaseDate", "2000-01-01");
        body.put("duration", 100);
        body.put("rating", "pg-13"); // ❌ строгое соответствие нарушено
        ArrayNode arr = body.putArray("genres");
        arr.add(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createFilm_missingRating_shouldReturn404() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "n");
        body.put("description", "d");
        body.put("releaseDate", "2000-01-01");
        body.put("duration", 100);
        ArrayNode arr = body.putArray("genres");
        arr.add(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_emptyGenres_shouldFail() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "n");
        body.put("description", "d");
        body.put("releaseDate", "2000-01-01");
        body.put("duration", 100);
        body.put("rating", "PG");
        body.putArray("genres"); // пусто

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_genreNotFound_shouldFail() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "n");
        body.put("description", "d");
        body.put("releaseDate", "2000-01-01");
        body.put("duration", 100);
        body.put("rating", "PG");
        ArrayNode arr = body.putArray("genres");
        arr.add(999L);

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createFilm_emptyName_shouldFail() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "");
        body.put("description", "d");
        body.put("releaseDate", "2000-01-01");
        body.put("duration", 100);
        body.put("rating", "PG");
        ArrayNode arr = body.putArray("genres");
        arr.add(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_descriptionTooLong_shouldFail() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "n");
        body.put("description", "x".repeat(201)); // >200
        body.put("releaseDate", "2000-01-01");
        body.put("duration", 100);
        body.put("rating", "PG");
        ArrayNode arr = body.putArray("genres");
        arr.add(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_releaseBeforeCinema_shouldFail() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "n");
        body.put("description", "d");
        body.put("releaseDate", "1895-12-27");
        body.put("duration", 100);
        body.put("rating", "PG");
        ArrayNode arr = body.putArray("genres");
        arr.add(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_nonPositiveDuration_shouldFail() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "n");
        body.put("description", "d");
        body.put("releaseDate", "2000-01-01");
        body.put("duration", 0);
        body.put("rating", "PG");
        ArrayNode arr = body.putArray("genres");
        arr.add(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    //Update

    @Test
    void updateFilm_updateRatingAndGenres_ok() throws Exception {
        long id = createFilmAndGetId(
                "A", "d", LocalDate.of(2000,1,1), 100,
                "PG", new LinkedHashSet<>(Set.of(1L))
        );

        ObjectNode upd = objectMapper.createObjectNode();
        upd.put("id", id);
        upd.put("name", "A1");
        upd.put("rating", "R");
        ArrayNode arr = upd.putArray("genres");
        arr.add(4L); // Триллер
        arr.add(6L); // Боевик

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("A1"))
                .andExpect(jsonPath("$.rating").value("R"))
                .andExpect(jsonPath("$.genres.length()").value(2))
                .andExpect(jsonPath("$.genres[0]").value(4))
                .andExpect(jsonPath("$.genres[1]").value(6));
    }

    //Likes

    @Test
    void addLike_ok() throws Exception {
        long filmId = createFilmAndGetId(
                "Blade Runner", "desc", LocalDate.of(1982, 6, 25), 117,
                "R", new LinkedHashSet<>(Set.of(4L))
        );
        long userId = createUserAndGetId("deckard@example.com", "deckard", "Deckard", LocalDate.of(1980, 1, 1));

        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        // идемпотентность
        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }

    @Test
    void removeLike_ok() throws Exception {
        long filmId = createFilmAndGetId(
                "Inception", "desc", LocalDate.of(2010, 7, 16), 148,
                "PG-13", new LinkedHashSet<>(Set.of(4L))
        );
        long userId = createUserAndGetId("cobb@example.com", "cobb", "Cobb", LocalDate.of(1990, 1, 1));

        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.delete("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        // повторный DELETE — идемпотентен
        mockMvc.perform(MockMvcRequestBuilders.delete("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }

    //Popular

    @Test
    void getTopFilms_ok() throws Exception {
        long a = createFilmAndGetId("A", "d", LocalDate.of(2000,1,1), 100, "PG", new LinkedHashSet<>(Set.of(1L)));
        long b = createFilmAndGetId("B", "d", LocalDate.of(2000,1,1), 100, "PG", new LinkedHashSet<>(Set.of(1L)));
        long c = createFilmAndGetId("C", "d", LocalDate.of(2000,1,1), 100, "PG", new LinkedHashSet<>(Set.of(1L)));

        long u1 = createUserAndGetId("a@ex.com", "a", "A", LocalDate.of(1990,1,1));
        long u2 = createUserAndGetId("b@ex.com", "b", "B", LocalDate.of(1990,1,1));
        long u3 = createUserAndGetId("c@ex.com", "c", "C", LocalDate.of(1990,1,1));

        // A: 2 лайка, B: 1 лайк, C: 0
        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", a, u1)).andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", a, u2)).andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.put("/films/{id}/like/{userId}", b, u3)).andExpect(status().isOk());

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
