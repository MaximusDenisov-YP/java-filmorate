package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
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
    private final FilmLikeStorage likeStorage;
    private final MpaStorage mpaDbStorage;
    private final GenreStorage genreDbStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            FilmLikeStorage likeStorage, MpaStorage mpaDbStorage,
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
        film.setMpa(resolveMpa(film.getMpa()).get());
        film.setGenres(resolveGenres(film.getGenres()));
        Film result = filmStorage.createFilm(film);
        log.info("Фильм создан: {}", result);
        return result;
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
        filmStorage.deleteFilm(id);
        log.info("Фильм удалён c ID: {}", id);
    }

    public List<Film> getPopularFilmsFiltered(Integer count, Integer genreId, Integer year) {
        int limit = (count != null && count > 0) ? count : 10;

        if (genreId == null && year == null) {
            return filmStorage.getPopularFilms(limit);
        }

        if (genreId != null && genreDbStorage.getById(genreId).isEmpty()) {
            throw new NotFoundException("Жанр с ID %d не найден".formatted(genreId));
        }

        return filmStorage.getPopularFilmsByGenreAndYear(count, genreId, year);
    }

    public void addLike(long filmId, long userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId)));

        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=%d не найден".formatted(filmId)));

        likeStorage.addFilmLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId)));

        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=%d не найден".formatted(filmId)));

        likeStorage.removeFilmLike(filmId, userId);
    }

    public List<Genre> getAllGenres() {
        return genreDbStorage.getAll();
    }

    public Optional<Genre> getGenreById(int id) {
        return Optional.ofNullable(genreDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Жанр фильма с ID %d - не существует!".formatted(id))));
    }

    public List<Mpa> getAllRatings() {
        return mpaDbStorage.getAll();
    }

    public Mpa getRatingById(int id) {
        return mpaDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Возрастного рейтинга с ID %d - не существует!".formatted(id)));
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
        if (film.getMpa().getId() > mpaDbStorage.getAll().size()) {
            throw new NotFoundException("Запрашиваемого возрастного рейтинга - не существует!");
        }
    }

    private Optional<Mpa> resolveMpa(Mpa mpa) {
        return mpa == null ? Optional.of(new Mpa(0, "Not Rated")) : mpaDbStorage.getById(mpa.getId());
    }

    private List<Genre> resolveGenres(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) return List.of();

        List<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .distinct()
                .toList();

        List<Genre> resolvedGenres = genreDbStorage.getByIds(genreIds);

        if (resolvedGenres.size() != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены");
        }

        return resolvedGenres;
    }

}

