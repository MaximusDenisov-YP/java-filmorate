package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class User {

    private long id;
    @Email
    private String email;
    @NotNull
    @Pattern(regexp = "\\S+", message = "Имя пользователя не должно содержать пробелы")
    private String login;
    @Size(min = 1, max = 20)
    private String name;
    @NotNull
    private LocalDate birthday;
    private Set<Long> friends;
}
