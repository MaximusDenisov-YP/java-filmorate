package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;


@RestController
@RequestMapping("films")
@Validated
public class FilmController {
    private final FilmService filmService;
    private final DirectorService directorService;

    public FilmController(FilmService filmService, DirectorService directorService) {
        this.filmService = filmService;
        this.directorService = directorService;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Positive long id) {
        return filmService.getFilmById(id);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable @Positive long id) {
        filmService.deleteFilm(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilmsFiltered(count, genreId, year);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@Positive @PathVariable long id, @Positive @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@Positive @PathVariable long id, @Positive @PathVariable long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(
            @PathVariable long directorId,
            @RequestParam(defaultValue = "year") String sortBy) {
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            throw new ValidationException("Некорректный параметр сортировки. Используйте 'year' либо 'likes'");
        }
        return directorService.getSortedFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(
            @RequestParam @Positive(message = "userId должен быть положительным") Long userId,
            @RequestParam @Positive(message = "friendId должен быть положительным") Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title") String by) {

        String searchQuery = query.toLowerCase();
        String[] searchBy = by.split(",");

        List<Film> result = new ArrayList<>();

        boolean searchTitle = false;
        boolean searchDirector = false;

        for (String searchType : searchBy) {
            switch (searchType.trim().toLowerCase()) {
                case "title":
                    searchTitle = true;
                    break;
                case "director":
                    searchDirector = true;
                    break;
                default:
                    throw new IllegalArgumentException("Некоректный параметр поиска: " + searchType);
            }
        }

        if (searchTitle && searchDirector) {
            List<Film> byTitle = filmService.searchFilmsByTitle(searchQuery);
            List<Film> byDirector = filmService.searchFilmsByDirector(searchQuery);

            Map<Long, Film> filmsMap = new HashMap<>();
            byTitle.forEach(f -> filmsMap.put(f.getId(), f));
            byDirector.forEach(f -> filmsMap.put(f.getId(), f));

            result.addAll(filmsMap.values());
        } else if (searchTitle) {
            result.addAll(filmService.searchFilmsByTitle(searchQuery));
        } else if (searchDirector) {
            result.addAll(filmService.searchFilmsByDirector(searchQuery));
        }

        result.sort(Comparator.comparingInt(Film::getLikesCount).reversed());
        return result;
    }
}