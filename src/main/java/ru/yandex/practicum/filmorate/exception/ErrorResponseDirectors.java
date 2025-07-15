package ru.yandex.practicum.filmorate.exception;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ErrorResponseDirectors {
    int code;
    String error;
}
