package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.services.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Mpa> findAll() {
        return filmService.getAllMpa();
    }

    @GetMapping("/{id}")
    public Mpa getById(@PathVariable @Positive Long id) {
        return filmService.getMpa(id);
    }
}