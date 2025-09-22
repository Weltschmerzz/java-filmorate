package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"id"})
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private Instant birthday;
}