package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.services.FilmService;

import java.util.Collection;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable @Positive Long id) {
        filmService.deleteById(id);
        return Map.of("Удален филь с id ", String.valueOf(id));
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Map<String, String> addLike(@PathVariable @Positive Long id,
                                       @PathVariable @Positive Long userId) {
        Film film = filmService.addLike(id, userId);
        return Map.of("Пользователь с id: " + userId + " поставил лайк фильму ", String.valueOf(film.getName()));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Map<String, String> removeLike(@PathVariable @Positive Long id,
                                          @PathVariable @Positive Long userId) {
        Film film = filmService.removeLike(id, userId);
        return Map.of("Пользователь с id: " + userId + " удалил лайк фильму ", String.valueOf(film.getName()));
    }

    @GetMapping(value = "/popular", params = "count")
    public Collection<Film> getTopFilms(@RequestParam(defaultValue = "10")
                                        @NotNull(message = "count должен быть указан")
                                        @Positive(message = "count должен быть положительным")
                                        int count) {
        return filmService.getTopFilms(count);
    }
}
