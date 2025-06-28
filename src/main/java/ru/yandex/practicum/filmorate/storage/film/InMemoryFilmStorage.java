package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(long id) {
        if (films.get(id) == null) {
            throw new NotFoundException(String.format("Фильм с ID %d - не существует", id));
        }
        return films.get(id);
    }

    @Override
    public Film createFilm(Film film) {
        validateFilm(film);
        film.setId(getNextId());
        film.setUsersLikes(new HashSet<>());
        films.put(film.getId(), film);
        log.info("Фильм создан! " + film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validateFilm(film);
        Film currentFilm = films.get(film.getId());
        if (currentFilm == null) {
            log.warn("Обновление не выполнено — фильм с ID={} не найден", film.getId());
            throw new NotFoundException("Фильма с таким ID не существует!");
        }
        currentFilm.setName(film.getName());
        currentFilm.setDescription(film.getDescription());
        currentFilm.setReleaseDate(film.getReleaseDate());
        currentFilm.setDuration(film.getDuration());
        log.info("Пользователь с ID={} обновлён: {}", currentFilm.getId(), currentFilm);
        return currentFilm;
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        Comparator<Film> comparator = Comparator.comparing(film -> film.getUsersLikes().size());
        return films.values().stream()
                .sorted(comparator.reversed())
                .limit(count)
                .toList();
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)))
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
