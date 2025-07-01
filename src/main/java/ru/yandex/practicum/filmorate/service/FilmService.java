package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage")UserStorage userStorage,
            LikeStorage likeStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeStorage;
    }

    public void addLike(long filmId, long userId) {
        userStorage.getUserById(userId); // валидация
        filmStorage.getFilmById(filmId);
        likeStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        userStorage.getUserById(userId); // валидация
        filmStorage.getFilmById(filmId);
        likeStorage.removeLike(filmId, userId);
    }
}

