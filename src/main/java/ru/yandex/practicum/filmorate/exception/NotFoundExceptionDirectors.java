package ru.yandex.practicum.filmorate.exception;

public class NotFoundExceptionDirectors extends RuntimeException {
    public NotFoundExceptionDirectors(String message) {
        super(message);
    }
}
