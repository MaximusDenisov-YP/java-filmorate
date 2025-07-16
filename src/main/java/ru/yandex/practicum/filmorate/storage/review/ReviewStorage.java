package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Optional<Review> getReviewById(long id);

    Collection<Review> getReviews(long count);

    Collection<Review> getReviewsByFilmId(long filmId, long count);

    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReview(long id);

    void deleteReviewsByUserId(long userId);
}
