package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Map;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    void deleteById(Long id);

    Film getById(Long id);

    Collection<Film> findAll();

    Genre getGenreById(Long id);

    Map<Long, Genre> getAllGenres();

    boolean isGenreExist(Long id);

    Mpa getMpaById(Long id);

    Map<Long, Mpa> getAllMpa();

    boolean isMpaExist(Long id);
}