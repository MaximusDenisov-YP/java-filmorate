package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Slf4j
public class ReviewLikeDbStorage implements ReviewLikeStorage {
    private final JdbcTemplate jdbcTemplate;

    public ReviewLikeDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addReviewLike(long reviewId, long userId, boolean isPositive) {
        String sql = "INSERT INTO reviews_likes (review_id, user_id, is_positive) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, isPositive);
    }

    @Override
    public void removeReviewLike(long reviewId, long userId) {
        String sql = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);
        log.info("Удалён лайк юзера с ID {} у отзыва с ID {}", userId, reviewId);
    }

    @Override
    public void removeReviewDislike(long reviewId, long userId) {
        String sql = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ? AND is_positive = false";
        jdbcTemplate.update(sql, reviewId, userId);
        log.info("Удалён дизлайк юзера с ID {} у отзыва с ID {}", userId, reviewId);
    }

    @Override
    public List<ReviewLike> getReviewLikesByReviewId(long reviewId) {
        String sql = "SELECT * FROM reviews_likes WHERE review_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToReviewLike, reviewId);
    }

    private ReviewLike mapRowToReviewLike(ResultSet rs, int rowNum) throws SQLException {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setId(rs.getLong("id"));
        reviewLike.setReviewId(rs.getLong("review_id"));
        reviewLike.setUserId(rs.getLong("user_id"));
        reviewLike.setIsPositive(rs.getBoolean("is_positive"));
        return reviewLike;
    }
}
