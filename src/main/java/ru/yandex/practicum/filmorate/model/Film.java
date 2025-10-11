package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@ToString
@Setter
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Long id;
    private String name;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Genre> genres = new LinkedHashSet<>();
    private Mpa mpa;
    private Set<Long> likes = new HashSet<>();
}
