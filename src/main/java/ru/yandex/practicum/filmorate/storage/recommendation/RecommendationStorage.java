package ru.yandex.practicum.filmorate.storage.recommendation;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface RecommendationStorage {
    boolean hasUserLikes(long userId);

    List<Long> findTopSimilarUsers(long userId);

    List<Film> findRecommendedFilms(long userId, List<Long> similarUserIds);
}