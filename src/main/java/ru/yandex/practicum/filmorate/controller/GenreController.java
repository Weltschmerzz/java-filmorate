package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.services.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Genre> findAll() {
        return filmService.getAllGener();
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable @Positive Long id) {
        return filmService.getGenre(id);
    }
}