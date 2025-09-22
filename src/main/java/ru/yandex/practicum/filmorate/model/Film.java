package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Instant;


@Getter
@ToString
@Setter
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Long id;
    private String name;
    private String description;
    Instant releaseDate;
    Integer duration;
}
