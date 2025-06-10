package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан! " + film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        validateFilm(film);
        Film currentFilm = films.get(film.getId());
        if (currentFilm == null) {
            log.warn("Обновление не выполнено — фильм с ID={} не найден", film.getId());
            throw new ValidationException("Фильма с таким ID не существует!");
        }
        currentFilm.setName(film.getName());
        currentFilm.setDescription(film.getDescription());
        currentFilm.setReleaseDate(film.getReleaseDate());
        currentFilm.setDuration(film.getDuration());
        log.info("Пользователь с ID={} обновлён: {}", currentFilm.getId(), currentFilm);
        return currentFilm;
    }

    public void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank())
            throw new ValidationException("Название фильма не может быть пустым");

        if (film.getDescription().length() > 200)
            throw new ValidationException(String.format("Максимальная длина описания — %d символов.\nВаша длина описания — %d.", 200, film.getDescription().length()));

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)))
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");

        if (film.getDuration() <= 0)
            throw new ValidationException("Продолжительность фильма должна быть положительной величиной");
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
