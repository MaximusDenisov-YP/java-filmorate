package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.util.Collection;

public interface ReviewLikeStorage {
    void addReviewLike(long reviewId, long userId, boolean isPositive);

    void removeReviewLike(long reviewId, long userId);

    void removeReviewDislike(long reviewId, long userId);

    Collection<ReviewLike> getReviewLikesByReviewId(long reviewId);
}