package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final ReviewLikeStorage reviewLikeStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final EventStorage eventStorage;

    public ReviewService(ReviewStorage reviewStorage, ReviewLikeStorage reviewLikeStorage, FilmStorage filmStorage,
                         UserStorage userStorage, EventStorage eventStorage) {
        this.reviewStorage = reviewStorage;
        this.reviewLikeStorage = reviewLikeStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
    }

    public Collection<Review> getReviews(long count) {
        return reviewStorage.getReviews(count);
    }

    public Review getReviewById(Long id) {
        Review review = reviewStorage.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID %d не найден".formatted(id)));
        log.info("Получили по запросу отзыв под ID {}", id);
        return review;
    }

    public Collection<Review> getReviewsByFilmId(long filmId, long count) {
        validateReview(filmId, null);
        Collection<Review> reviews = reviewStorage.getReviewsByFilmId(filmId, count);
        log.info("Получены отзывы по filmId = {}\n{}", filmId, reviews);
        return reviews;
    }

    public Review createReview(Review review) {
        validateReview(review.getFilmId(), review.getUserId());
        Review result = reviewStorage.createReview(review);
        eventStorage.createEvent(new Event(review.getUserId(), Event.EventType.REVIEW, Event.Operation.ADD,
                review.getReviewId()));
        log.info("Создан отзыв {}", result);
        return result;
    }

    public Review updateReview(Review review) {
        validateReview(review.getFilmId(), review.getUserId());

        log.info("Мы обновляем отзыв {}\nНа отзыв {}", reviewStorage.getReviewById(review.getUserId()), review);
        Review result = reviewStorage.updateReview(review);
        eventStorage.createEvent(new Event(result.getUserId(), Event.EventType.REVIEW, Event.Operation.UPDATE,
                review.getReviewId()));
        log.info("Обновлён отзыв {}", result);
        return result;
    }

    public void deleteReview(long reviewId) {
        Optional<Review> optionalReview = reviewStorage.getReviewById(reviewId);
        reviewStorage.deleteReview(reviewId);
        optionalReview.ifPresent(review ->
                eventStorage.createEvent(new Event(review.getUserId(), Event.EventType.REVIEW, Event.Operation.REMOVE,
                reviewId)));
        log.info("Удалён отзыв с ID {}", reviewId);
    }

    public void setLike(long reviewId, long userId) {
        if (hasUserLike(reviewId, userId)) {
            reviewLikeStorage.removeReviewLike(reviewId, userId);
//            recalcLikesForReview(reviewId);
        }
        reviewLikeStorage.addReviewLike(reviewId, userId, true);
//        recalcLikesForReview(reviewId);
        log.info("Поставлен лайк отзыву с ID {} от пользователя с ID {}", reviewId, userId);
    }

    public void setDislike(long reviewId, long userId) {
        if (hasUserLike(reviewId, userId)) {
            reviewLikeStorage.removeReviewLike(reviewId, userId);
//            recalcLikesForReview(reviewId);
        }
        reviewLikeStorage.addReviewLike(reviewId, userId, false);
//        recalcLikesForReview(reviewId);
        log.info("Поставлен дизлайк отзыву с ID {} от пользователя с ID {}", reviewId, userId);
    }

    public void deleteLike(long reviewId, long userId) {
        reviewLikeStorage.removeReviewLike(reviewId, userId);
//        recalcLikesForReview(reviewId);
        log.info("Удалён лайк у отзыва с ID {} от пользователя с ID {}", reviewId, userId);
    }

    public void deleteDislike(long reviewId, long userId) {
        reviewLikeStorage.removeReviewDislike(reviewId, userId);
//        recalcLikesForReview(reviewId);
        log.info("Удалён дизлайк у отзыва с ID {} от пользователя с ID {}", reviewId, userId);
    }

    private boolean hasUserLike(long reviewId, long userId) {
        return reviewLikeStorage.getReviewLikesByReviewId(reviewId)
                .stream()
                .anyMatch(like -> like.getUserId() == userId);
    }

    public void deleteReviewsByUserId(long userId) {
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь для удаления отзывов не найден");
        }
        reviewStorage.deleteReviewsByUserId(userId);
        log.info("Удалёны отзывы от пользователя с ID {}", userId);
    }

//    private void recalcLikesForReview(long reviewId) {
//        List<ReviewLike> likes = (List<ReviewLike>) reviewLikeStorage.getReviewLikesByReviewId(reviewId);
//        int useful = likes.stream()
//                .mapToInt(like -> like.getIsPositive() ? 1 : -1)
//                .sum();
//        Review review = reviewStorage.getReviewById(reviewId)
//                .orElseThrow(() -> new NotFoundException("Отзыв с ID %d не найден".formatted(reviewId)));
//        review.setUseful(useful);
//        reviewStorage.updateReview(review);
//    }

    private void validateReview(long filmId, @Nullable Long userId) {
        List<Film> films = (List<Film>) filmStorage.getFilms();
        List<User> users = (List<User>) userStorage.getUsers();
        boolean filmExists = films.stream()
                .anyMatch(film -> film.getId() == filmId);
        if (!filmExists) {
            throw new NotFoundException("Фильм с ID %d не найден".formatted(filmId));
        }
        if (userId != null) {
            boolean userExists = users.stream()
                    .anyMatch(user -> user.getId() == userId);
            if (!userExists) {
                throw new NotFoundException("Пользователь с ID %d не найден".formatted(userId));
            }
        }
    }
}
