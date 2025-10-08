package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends BaseControllerTest {

    @Test
    void createUser_ok() throws Exception {
        User u = new User();
        u.setEmail("john@example.com");
        u.setLogin("john");
        u.setName("John");
        u.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/users")
                                .contentType(json)
                                .content(toJson(u))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.login").value("john"))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void createUser_blankName_becomesLogin() throws Exception {
        User u = new User();
        u.setEmail("jane@example.com");
        u.setLogin("jane");
        u.setName("   ");
        u.setBirthday(LocalDate.of(1992, 5, 5));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/users")
                                .contentType(json)
                                .content(toJson(u))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("jane"));
    }

    @Test
    void createUser_badEmail_shouldFail() throws Exception {
        User u = new User();
        u.setEmail("bad.email");
        u.setLogin("user1");
        u.setName("User");
        u.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void createUser_loginWithSpace_shouldFail() throws Exception {
        User u = new User();
        u.setEmail("good@mail.com");
        u.setLogin("bad login");
        u.setName("User");
        u.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void createUser_futureBirthday_shouldFail() throws Exception {
        User u = new User();
        u.setEmail("fut@mail.com");
        u.setLogin("fut");
        u.setName("Future");
        u.setBirthday(LocalDate.of(2990, 1, 1)); // заведомо будущее

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_notFound() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/{id}", 9999)
        ).andExpect(status().isNotFound());
    }

    @Test
    void findAll_returnsCollection() throws Exception {
        User u = new User();
        u.setEmail("a@a.com");
        u.setLogin("a");
        u.setName("A");
        u.setBirthday(LocalDate.of(1990, 1, 1));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u))
        ).andExpect(status().isCreated());

        u = new User();
        u.setEmail("b@b.com");
        u.setLogin("b");
        u.setName("B");
        u.setBirthday(LocalDate.of(1991, 1, 1));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u))
        ).andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    void addFriend_mutualAndGetFriends_ok() throws Exception {
        long id1 = createUserAndGetId("john@example.com", "john", "John", LocalDate.of(1990, 1, 1));
        long id2 = createUserAndGetId("jane@example.com", "jane", "Jane", LocalDate.of(1992, 5, 5));

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/{id}/friends/{friendId}", id1, id2)
        ).andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/friends", id1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0].id").value((int) id2))
                .andExpect(jsonPath("$[1]").doesNotExist());

        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/friends", id2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0].id").value((int) id1))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void removeFriend_mutualRemoval_ok() throws Exception {
        long id1 = createUserAndGetId("a@a.com", "a", "A", LocalDate.of(1990, 1, 1));
        long id2 = createUserAndGetId("b@b.com", "b", "B", LocalDate.of(1991, 2, 2));

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/{id}/friends/{friendId}", id1, id2)
        ).andExpect(status().isOk());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/users/{id}/friends/{friendId}", id1, id2)
        ).andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/friends", id1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0]").doesNotExist());

        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/friends", id2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0]").doesNotExist());
    }

    @Test
    void getCommonFriends_oneCommon_ok() throws Exception {
        // создаём трёх пользователей: A, B и C (C — их общий друг)
        long aId = createUserAndGetId("a@a.com", "a", "A", LocalDate.of(1990, 1, 1));
        long bId = createUserAndGetId("b@b.com", "b", "B", LocalDate.of(1991, 2, 2));
        long cId = createUserAndGetId("c@c.com", "c", "C", LocalDate.of(1992, 3, 3));

        // A дружит с C
        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/{id}/friends/{friendId}", aId, cId)
        ).andExpect(status().isOk());

        // B дружит с C
        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/{id}/friends/{friendId}", bId, cId)
        ).andExpect(status().isOk());

        // Общие друзья A и B — должен быть только C
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{id}/friends/common/{otherId}", aId, bId)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0].id").value((int) cId))
                .andExpect(jsonPath("$[1]").doesNotExist()); // лишних элементов нет
    }

    @Test
    void getCommonFriends_none_shouldReturn404() throws Exception {
        long dId = createUserAndGetId("d@d.com", "d", "D", LocalDate.of(1993, 4, 4));
        long eId = createUserAndGetId("e@e.com", "e", "E", LocalDate.of(1994, 5, 5));

        // У D и E нет общих друзей
        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/{id}/friends/common/{otherId}", dId, eId)
        ).andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ok() throws Exception {
        long id = createUserAndGetId("john@example.com", "john", "John", LocalDate.of(1990, 1, 1));

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/users/{id}", id)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_notFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
