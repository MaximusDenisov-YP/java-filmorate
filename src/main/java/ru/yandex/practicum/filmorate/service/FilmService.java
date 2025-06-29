package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.storage.UserStorage;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(long filmId, long userId) {
        filmStorage.getFilmById(filmId)
                .getUsersLikes()
                .add(userStorage.getUserById(userId).getId());
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.getFilmById(filmId)
                .getUsersLikes()
                .remove(userStorage.getUserById(userId).getId());
    }
}
