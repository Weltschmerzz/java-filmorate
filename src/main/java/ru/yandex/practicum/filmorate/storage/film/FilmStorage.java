package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    void deleteById(Long id);

    Film getById(Long id);

    Collection<Film> findAll();

    boolean isGenreExist(Long id);

    boolean isRatingExist(String rating);
}