package ru.yandex.practicum.filmorate.validation;

public interface DomainValidator<T> {
    void validateCreate(T object);
    void validateUpdate(T object);
}
