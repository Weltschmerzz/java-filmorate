package ru.yandex.practicum.filmorate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.DomainValidator;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class FilmService {

    private final Map<Long, Film> films = new HashMap<>();
    private Long idCounter = 1L;
    private final DomainValidator<Film> validator = new FilmValidator();

    public Film create(Film film) {
        log.info("Запрос на создание фильма получен: {}", film);
        validator.validateCreate(film);
        Long id = generateNextId();
        film.setId(id);
        films.put(id, film);
        log.info("Запрос на создание фильма выполнен: {}", film);
        return film;
    }

    public Collection<Film> findAll() {
        log.info("Запрос на вывод всех фильмов по findAll()");
        return films.values();
    }

    public Film getFilmById(Long id) {
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


    public Film update(Film newFilm) {
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

        validator.validateUpdate(newFilm);

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
}
