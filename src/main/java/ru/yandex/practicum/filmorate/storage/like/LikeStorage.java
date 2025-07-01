package ru.yandex.practicum.filmorate.storage.like;

import java.util.List;

public interface LikeStorage {
    void addLike(long filmId, long userId);
    void removeLike(long filmId, long userId);
    List<Long> getLikesByFilmId(long filmId);
}