package ru.yandex.practicum.filmorate.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleConditionsNotMet(NotFoundException ex, WebRequest req) {
        log.warn("Ошибка поиска данных: {}", ex.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        Map<String, Object> body = errorBody(status, ex.getMessage(), req);
        return ResponseEntity
                .status(status)
                .body(body);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleConditionsNotMet(ValidationException ex, WebRequest req) {
        log.warn("Ошибка валидации данных: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, Object> body = errorBody(status, ex.getMessage(), req);
        return ResponseEntity
                .status(status)
                .body(body);
    }

    // Унифицированный формат ошибки
    private Map<String, Object> errorBody(HttpStatus status, String message, WebRequest req) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", req.getDescription(false).replace("uri=", "")
        );
    }
}
