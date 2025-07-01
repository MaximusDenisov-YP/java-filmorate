package ru.yandex.practicum.filmorate.storage.like;

import java.util.List;

public class InMemoryLikeStorage implements LikeStorage {
    @Override
    public void addLike(long filmId, long userId) {

    }

    @Override
    public void removeLike(long filmId, long userId) {

    }

    @Override
    public List<Long> getLikesByFilmId(long filmId) {
        return null;
    }
}
