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
        u.setEmail("john_ok@example.com");
        u.setLogin("john_ok");
        u.setName("John Ok");
        u.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/users")
                                .contentType(json)
                                .content(toJson(u))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("john_ok@example.com"))
                .andExpect(jsonPath("$.login").value("john_ok"))
                .andExpect(jsonPath("$.name").value("John Ok"));
    }

    @Test
    void createUser_blankName_becomesLogin() throws Exception {
        User u = new User();
        u.setEmail("jane_blank@example.com");
        u.setLogin("jane_blank");
        u.setName("   "); // должно замениться на login
        u.setBirthday(LocalDate.of(1992, 5, 5));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/users")
                                .contentType(json)
                                .content(toJson(u))
                )
                .andExpect(status().isCreated())
                // сервис обязан был подставить login вместо пустого name
                .andExpect(jsonPath("$.name").value("jane_blank"));
    }

    @Test
    void createUser_badEmail_shouldFail() throws Exception {
        User u = new User();
        u.setEmail("bad.email"); // невалидно
        u.setLogin("bad_email_login");
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
        u.setEmail("spacey@example.com");
        u.setLogin("bad login"); // пробелы запрещены
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
        u.setEmail("future@example.com");
        u.setLogin("future_user");
        u.setName("Future");
        u.setBirthday(LocalDate.of(2990, 1, 1)); // будущее

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_notFound() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/{id}", 999999)
        ).andExpect(status().isNotFound());
    }

    @Test
    void findAll_returnsCollection() throws Exception {
        // создаём двух разных пользователей с уникальными email/login
        User u1 = new User();
        u1.setEmail("list_a@example.com");
        u1.setLogin("list_a");
        u1.setName("ListA");
        u1.setBirthday(LocalDate.of(1990, 1, 1));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u1))
        ).andExpect(status().isCreated());

        User u2 = new User();
        u2.setEmail("list_b@example.com");
        u2.setLogin("list_b");
        u2.setName("ListB");
        u2.setBirthday(LocalDate.of(1991, 1, 1));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u2))
        ).andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    void addFriend_mutualAndGetFriends_ok() throws Exception {
        long id1 = createUserAndGetId(
                "friend1@example.com",
                "friend1_login",
                "Friend One",
                LocalDate.of(1990, 1, 1)
        );
        long id2 = createUserAndGetId(
                "friend2@example.com",
                "friend2_login",
                "Friend Two",
                LocalDate.of(1992, 5, 5)
        );

        // id1 добавляет id2 в друзья
        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/{id}/friends/{friendId}", id1, id2)
        ).andExpect(status().isOk());

        // проверяем, что у id1 друг — id2
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/friends", id1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0].id").value((int) id2))
                .andExpect(jsonPath("$[1]").doesNotExist());

        // и что связь симметричная (id2 видит id1)
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/friends", id2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0].id").value((int) id1))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void removeFriend_mutualRemoval_ok() throws Exception {
        long id1 = createUserAndGetId(
                "rm1@example.com",
                "rm1_login",
                "RM1",
                LocalDate.of(1990, 1, 1)
        );
        long id2 = createUserAndGetId(
                "rm2@example.com",
                "rm2_login",
                "RM2",
                LocalDate.of(1991, 2, 2)
        );

        // связываем
        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/{id}/friends/{friendId}", id1, id2)
        ).andExpect(status().isOk());

        // разрываем
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/users/{id}/friends/{friendId}", id1, id2)
        ).andExpect(status().isOk());

        // проверяем, что у обоих больше нет друзей
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
        // создаём трёх юзеров: A, B и C
        long aId = createUserAndGetId(
                "cm_a@example.com",
                "cm_a_login",
                "CMA",
                LocalDate.of(1990, 1, 1)
        );
        long bId = createUserAndGetId(
                "cm_b@example.com",
                "cm_b_login",
                "CMB",
                LocalDate.of(1991, 2, 2)
        );
        long cId = createUserAndGetId(
                "cm_c@example.com",
                "cm_c_login",
                "CMC",
                LocalDate.of(1992, 3, 3)
        );

        // A дружит с C
        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/{id}/friends/{friendId}", aId, cId)
        ).andExpect(status().isOk());

        // B дружит с C
        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/{id}/friends/{friendId}", bId, cId)
        ).andExpect(status().isOk());

        // теперь общие друзья A и B — это только C
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{id}/friends/common/{otherId}", aId, bId)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0].id").value((int) cId))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void getCommonFriends_none_shouldReturn404() throws Exception {
        long dId = createUserAndGetId(
                "no_d@example.com",
                "no_d_login",
                "NoD",
                LocalDate.of(1993, 4, 4)
        );
        long eId = createUserAndGetId(
                "no_e@example.com",
                "no_e_login",
                "NoE",
                LocalDate.of(1994, 5, 5)
        );

        // у D и E намеренно нет общих друзей
        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/{id}/friends/common/{otherId}", dId, eId)
        ).andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ok() throws Exception {
        long id = createUserAndGetId(
                "del_user@example.com",
                "del_login",
                "DelUser",
                LocalDate.of(1990, 1, 1)
        );

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
