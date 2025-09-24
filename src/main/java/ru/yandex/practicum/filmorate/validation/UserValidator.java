package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserValidator implements DomainValidator<User> {

    @Override
    public void validateCreate(User user) {
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
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    @Override
    public void validateUpdate(User user) {
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
            if (user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        }
    }
}
