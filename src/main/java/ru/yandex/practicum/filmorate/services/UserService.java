package ru.yandex.practicum.filmorate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.DomainValidator;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;
    DomainValidator<User> validator = new UserValidator();

    public User create(User user) {
        log.info("Запрос на создание пользователя получен {}", user);
        validator.validateCreate(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        Long id = generateNextId();
        user.setId(id);
        users.put(id, user);
        log.info("Запрос на создание пользователя выполнен {}", user);
        return user;
    }

    public User update(User newUser) {
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

        validator.validateUpdate(newUser);

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

    public Collection<User> findAll() {
        log.info("Запрос на вывод всех пользователей");
        return users.values();
    }


    public User getUserById(Long id) {
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
}

