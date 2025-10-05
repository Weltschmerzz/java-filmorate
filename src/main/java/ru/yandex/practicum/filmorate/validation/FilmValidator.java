package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmValidator implements DomainValidator<Film> {
    @Override
    public void validateCreate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым!");
        }
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            throw new ValidationException("Поле description не может быть пустым!");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Допустимая длина поля description — 200 символов!");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана!");
        }
        if (film.getReleaseDate().isBefore(java.time.LocalDate.parse("1895-12-28"))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
        if (film.getDuration() == null) {
            throw new ValidationException("Продолжительность фильма должна быть указана!");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом!");
        }
    }

    @Override
    public void validateUpdate(Film film) {

        if (film.getName() != null && film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым!");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов!");
        }
        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
            }
        }
        if (film.getDuration() != null) {
            if (film.getDuration() <= 0) {
                throw new ValidationException("Продолжительность фильма должна быть положительным числом!");
            }
        }
    }
}

