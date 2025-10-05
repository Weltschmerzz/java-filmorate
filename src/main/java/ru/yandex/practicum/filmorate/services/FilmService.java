package ru.yandex.practicum.filmorate.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validation.DomainValidator;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final DomainValidator<Film> validator = new FilmValidator();
    private final UserService userService;

    public Film create(Film film) {
        validator.validateCreate(film);
        log.info("Запрос на создание фильма: {}", film);
        return filmStorage.create(film);
    }

    public Collection<Film> findAll() {
        log.info("Запрос на вывод всех фильмов");
        return filmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        if (id == null) {
            throw new ValidationException("id должен быть указан!");
        }
        log.info("Вывод фильма с id: {}", id);
        return filmStorage.getById(id);
    }

    public Film update(Film newFilm) {

        if (newFilm == null) {
            throw new ValidationException("Тело запроса не должно быть пустым!");
        }

        Long id = newFilm.getId();
        if (id == null) {
            throw new ValidationException("id должен быть указан!");
        }

        Film existedFilm = filmStorage.getById(newFilm.getId());
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

        log.info("Обновлён фильм через сервис: {}", existedFilm);
        return filmStorage.update(existedFilm);
    }

    public void deleteById(Long id) {
        log.info("Запрос на удаление фильма с id: {}", id);
        filmStorage.deleteById(id);
    }

    public Film addLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        User user = userService.getUserById(userId);

        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + "не найден!");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + "не найден!");
        }

        boolean filmChanged = film.getLikes().add(userId);
        boolean userChanged = user.getLikes().add(filmId);

        if (!filmChanged && !userChanged) {
            return film;
        }

        filmStorage.update(film);
        userService.update(user);

        log.info("Запрос на добавление лайка от пользователя {} фильму {}", user, film);
        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        User user = userService.getUserById(userId);

        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + "не найден!");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + "не найден!");
        }

        boolean filmChanged = film.getLikes().remove(userId);
        boolean userChanged = user.getLikes().remove(filmId);

        if (!filmChanged && !userChanged) {
            return film;
        }

        filmStorage.update(film);
        userService.update(user);
        log.info("Запрос на удаление лайка пользователя {} фильму {}", user, film);
        return film;
    }

    public Collection<Film> getTopFilms(int count) {
        log.info("Запрос на получение ТОП {} фильмов по популярности", count);
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

}
