package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;
    private final MpaStorage mpaDbStorage;
    private final GenreStorage genreDbStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            LikeStorage likeStorage,
            MpaStorage mpaDbStorage,
            GenreStorage genreDbStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeStorage;
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID %d не найден".formatted(id)));
    }

    public Film createFilm(Film film) {
        validateFilm(film, false);
        film.setMpa(resolveMpa(film.getMpa()));
        film.setGenres(resolveGenres(film.getGenres()));
        log.info("Фильм успешно создан: {}", film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм для обновления не найден");
        }
        validateFilm(film, true);
        log.info("Фильм обновлён: {}", film);
        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(Long id) {
        if (filmStorage.getFilmById(id).isEmpty()) {
            throw new NotFoundException("Фильм для удаления не найден");
        }
        log.info("Фильм удалён c ID: {}", id);
        filmStorage.deleteFilm(id);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public void addLike(long filmId, long userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId)));

        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=%d не найден".formatted(filmId)));

        likeStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId)));

        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=%d не найден".formatted(filmId)));

        likeStorage.removeLike(filmId, userId);
    }

    public List<Genre> getAllGenres() {
        return genreDbStorage.getAll();
    }

    public Optional<Genre> getGenreById(int id) {
        if (id > 6) {
            throw new NotFoundException(String.format("Жанра фильма с ID %s - не существует!", id));
        }
        return genreDbStorage.getById(id);
    }

    public List<Mpa> getAllRatings() {
        return mpaDbStorage.getAll();
    }

    public Mpa getRatingById(int id) {
        if (id > 5) {
            throw new NotFoundException(String.format("Возрастного рейтинга с ID %s - не существует!", id));
        }
        return mpaDbStorage.getById(id);
    }

    private void validateFilm(Film film, boolean isUpdate) {
        if (isUpdate && filmStorage.getFilmById(film.getId()).isEmpty()) {
            throw new NotFoundException("Такого фильма - не существует!");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE))
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");

        if (film.getMpa() == null) {
            film.setMpa(new Mpa(0, "Not Rated"));
        }
        if (film.getMpa().getId() > 5) {
            throw new NotFoundException("Запрашиваемого возрастного рейтинга - не существует!");
        }
    }

    private Mpa resolveMpa(Mpa mpa) {
        return mpa == null ? new Mpa(0, "Not Rated") : mpaDbStorage.getById(mpa.getId());
    }

    private List<Genre> resolveGenres(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) return List.of();
        for (Genre g : genres) {
            genreDbStorage.getById(g.getId()).orElseThrow(
                    () -> new NotFoundException("Жанр с id " + g.getId() + " не найден")
            );
        }
        return genres;
    }

}

