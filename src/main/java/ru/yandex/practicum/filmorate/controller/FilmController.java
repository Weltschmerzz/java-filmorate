package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private Long idCounter = 1L;

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Запрос на создание фильма получен: {}", film);
        validateCreate(film);
        Long id = generateNextId();
        film.setId(id);
        films.put(id, film);
        log.info("Запрос на создание фильма выполнен: {}", film);
        return film;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на вывод всех фильмов по findAll()");
        return films.values();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос на вывод фильма по id получен: {}", id);
        if (id == null) {
            throw new ValidationException("id должен быть указан!");
        }

        Film film = films.get(id);

        if (Objects.isNull(film)) {
            throw new NotFoundException(String.format("Не найден фильм с id: %d", id));
        }
        log.info("Запрос на фильмы по id выполнен: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Запрос на обновление фильма получен: {}", newFilm);
        if (newFilm == null) {
            throw new ValidationException("Тело запроса не должно быть пустым!");
        }

        Long id = newFilm.getId();
        if (id == null) {
            throw new ValidationException("id должен быть указан!");
        }

        Film existedFilm = films.get(id);
        if (Objects.isNull(existedFilm)) {
            throw new NotFoundException(String.format("Не найден фильм с id: %d", id));
        }

        validatePartial(newFilm);

        if (newFilm.getName() != null) {
            existedFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            existedFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            existedFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            existedFilm.setDuration(newFilm.getDuration());
        }

        films.put(id, existedFilm);
        log.info("Запрос на обновление фильма выполнен: {}", existedFilm);
        return existedFilm;
    }

    private Long generateNextId() {
        return idCounter++;
    }

    private void validateCreate(Film film) {
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
        if (film.getReleaseDate().isBefore(java.time.Instant.parse("1895-12-28T00:00:00Z"))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
        if (film.getDuration() == null) {
            throw new ValidationException("Продолжительность фильма должна быть указана!");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом!");
        }
    }

    private void validatePartial(Film film) {
        if (film.getName() != null && film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым!");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов!");
        }
        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(Instant.parse("1895-12-28T00:00:00Z"))) {
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
