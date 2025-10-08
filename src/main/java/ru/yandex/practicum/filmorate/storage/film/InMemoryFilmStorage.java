package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long idCounter = 1L;

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
}
