package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long idCounter = 1L;
    private final Map<Long, Genre> data = new LinkedHashMap<>() {{
        put(1L, new Genre(1L, "Комедия"));
        put(2L, new Genre(2L, "Драма"));
        put(3L, new Genre(3L, "Мультфильм"));
        put(4L, new Genre(4L, "Триллер"));
        put(5L, new Genre(5L, "Документальный"));
        put(6L, new Genre(6L, "Боевик"));
    }};
    private final List<String> acceptableRatingsList = Arrays.asList("G", "PG", "PG-13", "R", "NC-17");

    @Override
    public Film create(Film film) {
        long id = idCounter++;
        film.setId(id);
        films.put(id, film);
        log.info("Создан фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Long id = film.getId();

        if (id == null || !films.containsKey(id)) {
            throw new NotFoundException(String.format("Не найден фильм с id: %s", id));

        }

        films.put(id, film);
        log.info("Обновлён фильм: {}", film);
        return film;
    }

    @Override
    public void deleteById(Long id) {

        if (!films.containsKey(id)) {
            throw new NotFoundException(String.format("Не найден фильм с id: %s", id));
        }

        films.remove(id);
        log.info("Удалён фильм id={}", id);
    }

    @Override
    public Film getById(Long id) {
        Film film = films.get(id);

        if (film == null) {
            throw new NotFoundException(String.format("Не найден фильм с id: %s", id));
        }

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public boolean isGenreExist(Long id) {
        return data.containsKey(id);
    }

    @Override
    public boolean isRatingExist(String rating) {
        return acceptableRatingsList.contains(rating);
    }
}
