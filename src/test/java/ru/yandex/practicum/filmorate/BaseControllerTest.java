package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    protected MediaType json = MediaType.APPLICATION_JSON;

    protected String toJson(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @BeforeEach
    void clean() throws Exception {
        // Удаляем ВСЕ фильмы
        MvcResult filmsRes = mockMvc.perform(MockMvcRequestBuilders.get("/films"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode films = objectMapper.readTree(filmsRes.getResponse().getContentAsString());
        if (films.isArray()) {
            for (JsonNode f : films) {
                long id = f.get("id").asLong();
                mockMvc.perform(MockMvcRequestBuilders.delete("/films/{id}", id))
                        .andExpect(status().isOk());
            }
        }
    }

    @Test
    void test() throws Exception {

    }

    public long createUserAndGetId(String email, String login, String name, LocalDate birthday) throws Exception {
        User u = new User();
        u.setEmail(email);
        u.setLogin(login);
        u.setName(name);
        u.setBirthday(birthday);

        MvcResult res = mockMvc.perform(
                        MockMvcRequestBuilders.post("/users")
                                .contentType(json)
                                .content(toJson(u))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    public long createFilmAndGetId(String name, String description, LocalDate releaseDate, int duration) throws Exception {
        Film f = new Film();
        f.setName(name);
        f.setDescription(description);
        f.setReleaseDate(releaseDate);
        f.setDuration(duration);

        MvcResult res = mockMvc.perform(
                        MockMvcRequestBuilders.post("/films")
                                .contentType(json)
                                .content(toJson(f))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        return node.get("id").asLong();
    }
}