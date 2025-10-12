package ru.yandex.practicum.filmorate.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.validation.DomainValidator;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage = new InMemoryFilmStorage();
    private final DomainValidator<Film> validator = new FilmValidator(filmStorage);
    private final UserService userService;

    public Film create(Film film) {
        validator.validateCreate(film);
        Film normalized = normalize(film);
        log.info("Запрос на создание фильма: {}", normalized);
        return filmStorage.create(normalized);
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

        Film existedFilm = filmStorage.getById(id);
        if (Objects.isNull(existedFilm)) {
            throw new NotFoundException(String.format("Не найден фильм с id: %d", id));
        }

        validator.validateUpdate(newFilm);

        if (newFilm.getName() != null) existedFilm.setName(newFilm.getName());
        if (newFilm.getDescription() != null) existedFilm.setDescription(newFilm.getDescription());
        if (newFilm.getReleaseDate() != null) existedFilm.setReleaseDate(newFilm.getReleaseDate());
        if (newFilm.getDuration() != null) existedFilm.setDuration(newFilm.getDuration());
        if (newFilm.getGenres() != null) existedFilm.setGenres(normalizeGenres(newFilm.getGenres()));
        if (newFilm.getMpa() != null) existedFilm.setMpa(filmStorage.getMpaById(newFilm.getMpa().getId()));

        log.info("Обновлён фильм через сервис: {}", existedFilm);
        return filmStorage.update(existedFilm);
    }

    public void deleteById(Long id) {
        log.info("Запрос на удаление фильма с id: {}", id);
        filmStorage.deleteById(id);
    }

    public void addLike(Long filmId, Long userId) {
        LikeContext lc = likeValidate(filmId, userId);

        boolean filmChanged = lc.film().getLikes().add(userId);
        boolean userChanged = lc.user().getLikes().add(filmId);

        if (!filmChanged && !userChanged) {
            return;
        }

        filmStorage.update(lc.film());
        userService.update(lc.user());
        log.info("Запрос на добавление лайка от пользователя {} фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        LikeContext lc = likeValidate(filmId, userId);

        boolean filmChanged = lc.film().getLikes().remove(userId);
        boolean userChanged = lc.user().getLikes().remove(filmId);

        if (!filmChanged && !userChanged) {
            return;
        }

        filmStorage.update(lc.film());
        userService.update(lc.user());
        log.info("Запрос на удаление лайка пользователя {} фильму {}", userId, filmId);
    }

    public Collection<Film> getTopFilms(int count) {
        log.info("Запрос на получение ТОП {} фильмов по популярности", count);
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    public LikeContext likeValidate(Long filmId, Long userId) {
        Film film;
        try {
            film = filmStorage.getById(filmId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден!");
        }
        User user;
        try {
            user = userService.getUserById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден!");
        }
        return new LikeContext(film, user);
    }

    public record LikeContext(Film film, User user) {
    }

    public Genre getGenre(Long id) {
        if (filmStorage.getGenreById(id) == null) {
            throw new NotFoundException("Жанр не найден id=" + id);
        }
        return filmStorage.getGenreById(id);
    }

    public Collection<Genre> getAllGener() {
        return filmStorage.getAllGenres().values();
    }

    public Mpa getMpa(Long id) {
        if (filmStorage.getMpaById(id) == null) {
            throw new NotFoundException("MPA не найден id=" + id);
        }
        return filmStorage.getMpaById(id);
    }

    public Collection<Mpa> getAllMpa() {
        return filmStorage.getAllMpa().values();
    }

    private Film normalize(Film film) {
        Film copy = new Film();
        copy.setId(film.getId());
        copy.setName(film.getName());
        copy.setDescription(film.getDescription());
        copy.setReleaseDate(film.getReleaseDate());
        copy.setDuration(film.getDuration());
        copy.setMpa(filmStorage.getMpaById(film.getMpa().getId()));
        copy.setGenres(normalizeGenres(film.getGenres()));
        copy.setLikes(film.getLikes());
        return copy;
    }

    private LinkedHashSet<Genre> normalizeGenres(Set<Genre> input) {
        if (input == null || input.isEmpty()) return new LinkedHashSet<>();
        return input.stream()
                .filter(Objects::nonNull)
                .map(g -> filmStorage.getGenreById(g.getId()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingLong(Genre::getId))),
                        LinkedHashSet::new
                ));
    }
}
