package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Запрос на создание пользователя получен {}", user);
        validateCreate(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        Long id = generateNextId();
        user.setId(id);
        users.put(id, user);
        log.info("Запрос на создание пользователя выполнен {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Запрос на обновление пользователя получен: {}", newUser);
        if (newUser == null) {
            throw new ValidationException("Тело запроса не должно быть пустым!");
        }
        Long id = newUser.getId();
        if (id == null) {
            throw new ValidationException("id должен быть указан!");
        }
        User existing = users.get(id);
        if (existing == null) {
            throw new NotFoundException(String.format("Не найден пользователь с id: %d", id));
        }

        validatePartial(newUser);

        if (newUser.getLogin() != null) {
            existing.setLogin(newUser.getLogin());
        }
        if (newUser.getEmail() != null) {
            existing.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            if (newUser.getName().isBlank()) {
                existing.setName(existing.getLogin());
            } else {
                existing.setName(newUser.getName());
            }
        }
        if (newUser.getBirthday() != null) {
            existing.setBirthday(newUser.getBirthday());
        }

        users.put(id, existing);
        log.info("Запрос на обновление пользователя выполнен: {}", newUser);
        return existing;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на вывод всех пользователей");
        return users.values();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Запрос на вывод пользователя по id получен: {}", id);
        if (id == null) {
            throw new ValidationException("id должен быть указан");
        }
        User user = users.get(id);
        if (Objects.isNull(user)) {
            throw new NotFoundException(String.format("Не найден пользователь с id: %d", id));
        }
        log.info("Запрос на вывод пользователя по id выполнен: {}", user);
        return user;
    }

    private Long generateNextId() {
        return idCounter++;
    }

    private void validateCreate(User user) {
        if (user == null) throw new ValidationException("Тело запроса не должно быть пустым!");

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email: должен быть непустым и содержать '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный логин: не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(Instant.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void validatePartial(User user) {
        if (user.getEmail() != null) {
            String email = user.getEmail();
            if (email.isBlank() || !email.contains("@")) {
                throw new ValidationException("Некорректный email: должен быть непустым и содержать '@'");
            }
        }
        if (user.getLogin() != null) {
            String login = user.getLogin();
            if (login.isBlank() || login.contains(" ")) {
                throw new ValidationException("Некорректный логин: не может быть пустым и содержать пробелы");
            }
        }
        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(Instant.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        }
    }
}