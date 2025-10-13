package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    protected long createFilmAndGetId(
            String name,
            String description,
            LocalDate releaseDate,
            int duration,
            int mpaId,
            LinkedHashSet<Long> genreIds
    ) throws Exception {

        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", name);
        body.put("description", description);
        body.put("releaseDate", releaseDate.toString());
        body.put("duration", duration);

        ObjectNode mpa = objectMapper.createObjectNode();
        mpa.put("id", mpaId);
        body.set("mpa", mpa);

        ArrayNode genres = objectMapper.createArrayNode();
        if (genreIds != null) {
            for (Long gid : genreIds) {
                ObjectNode g = objectMapper.createObjectNode();
                g.put("id", gid);
                genres.add(g);
            }
        }
        body.set("genres", genres);

        MvcResult res = mockMvc.perform(post("/films")
                        .contentType(json)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(json))
                .andReturn();

        return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    protected String filmJson(String name, String description, LocalDate releaseDate, Integer duration,
                              String rating, Set<Long> genres) throws Exception {
        ObjectNode node = objectMapper.createObjectNode();
        if (name != null) node.put("name", name);
        if (description != null) node.put("description", description);
        if (releaseDate != null) node.put("releaseDate", releaseDate.toString());
        if (duration != null) node.put("duration", duration);
        if (rating != null) node.put("rating", rating);
        if (genres != null) {
            ArrayNode arr = node.putArray("genres");
            for (Long id : genres) {
                if (id == null) arr.addNull(); else arr.add(id);
            }
        }
        return objectMapper.writeValueAsString(node);
    }
}