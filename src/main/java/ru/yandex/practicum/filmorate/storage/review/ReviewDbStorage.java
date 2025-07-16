package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Review> getReviews(long count) {
        String query = """
                SELECT r.*, COALESCE(SUM(CASE
                 WHEN l.is_positive IS TRUE THEN 1
                  WHEN l.is_positive IS FALSE THEN -1
                  ELSE 0
                   END), 0) AS calculated_useful
                FROM reviews AS r
                LEFT JOIN reviews_likes l ON r.id = l.review_id
                GROUP BY r.id
                ORDER BY calculated_useful DESC, r.id
                LIMIT ?
                """;
        return jdbcTemplate.query(query, this::mapRowToReview, count);
    }

    @Override
    public Optional<Review> getReviewById(long id) {
        String query = """
                SELECT r.*, COALESCE(SUM(CASE
                 WHEN l.is_positive IS TRUE THEN 1
                  WHEN l.is_positive IS FALSE THEN -1
                  ELSE 0
                   END), 0) AS calculated_useful
                FROM reviews AS r
                LEFT JOIN reviews_likes l ON r.id = l.review_id
                WHERE r.id = ?
                GROUP BY r.id
                """;
        return jdbcTemplate.query(query, this::mapRowToReview, id)
                .stream()
                .findFirst();
    }

    @Override
    public Collection<Review> getReviewsByFilmId(long filmId, long count) {
        String query = """
                SELECT r.*, COALESCE(SUM(CASE
                 WHEN l.is_positive IS TRUE THEN 1
                  WHEN l.is_positive IS FALSE THEN -1
                  ELSE 0
                   END), 0) AS calculated_useful
                FROM reviews AS r
                LEFT JOIN reviews_likes l ON r.id = l.review_id
                WHERE film_id = ?
                GROUP BY r.id
                ORDER BY calculated_useful DESC, r.id
                LIMIT ?
                """;
        return jdbcTemplate.query(query, this::mapRowToReview, filmId, count);
    }

    @Override
    public Review createReview(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setLong(5, 0);
            return ps;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        return getReviewById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Ревью на обновление не найдено! ID = " + review.getReviewId()));
    }

    @Override
    public void deleteReview(long id) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Transactional
    public List<Long> deleteReviewsByUserId(long userId) {
        String getReviewsIds = "SELECT review_id FROM reviews_likes WHERE user_id = ?";
        String deleteReviewsLikes = "DELETE FROM reviews_likes WHERE user_id = ?";
        String deleteReviews = "DELETE FROM reviews WHERE user_id = ?";
        List<Long> reviewsIds = jdbcTemplate.query(getReviewsIds,
                (rs, rowNum) -> rs.getLong("review_id"), userId);
        jdbcTemplate.update(deleteReviewsLikes, userId);
        jdbcTemplate.update(deleteReviews, userId);
        return reviewsIds;
    }

    private List<ReviewLike> getReviewLikesByReviewId(long reviewId) {
        String sql = "SELECT * FROM reviews_likes WHERE review_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToReviewLike, reviewId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));

        List<ReviewLike> likes = getReviewLikesByReviewId(review.getReviewId());
        int useful = likes.stream()
                .mapToInt(like -> like.getIsPositive() ? 1 : -1)
                .sum();
        review.setUseful(useful);

        return review;
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