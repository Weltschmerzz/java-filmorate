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
                ).andExpect(status().isOk())
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
                ).andExpect(status().isOk())
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
        // создаём двух валидных пользователей — без безымянных блоков
        User u = new User();
        u.setEmail("a@a.com");
        u.setLogin("a");
        u.setName("A");
        u.setBirthday(LocalDate.of(1990, 1, 1));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u))
        ).andExpect(status().isOk());

        u = new User();
        u.setEmail("b@b.com");
        u.setLogin("b");
        u.setName("B");
        u.setBirthday(LocalDate.of(1991, 1, 1));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(json)
                        .content(toJson(u))
        ).andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }
}
