package ru.yandex.practicum.filmorate.exceptions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<Violation> violations) {
}