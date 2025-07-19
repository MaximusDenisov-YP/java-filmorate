package ru.yandex.practicum.filmorate.storage.film;

public interface FilmLikeStorage {
    void addFilmLike(long filmId, long userId);

    void removeFilmLike(long filmId, long userId);
}