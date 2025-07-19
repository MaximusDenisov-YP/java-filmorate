package ru.yandex.practicum.filmorate.storage.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecommendationDbStorage implements RecommendationStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean hasUserLikes(long userId) {
        String sql = "SELECT COUNT(*) FROM films_likes WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    @Override
    public List<Long> findTopSimilarUsers(long userId) {
        String sql = """
                SELECT l2.user_id AS similar_user, COUNT(*) AS common_likes_count
                FROM films_likes l1
                JOIN films_likes l2 ON l1.film_id = l2.film_id AND l1.user_id != l2.user_id
                WHERE l1.user_id = ?
                GROUP BY l2.user_id
                ORDER BY common_likes_count DESC
                LIMIT 5
                """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("similar_user"),
                userId);
    }

    @Override
    public List<Film> findRecommendedFilms(long userId, List<Long> similarUserIds) {
        if (similarUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = """
                SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating, m.id AS mpa_id, m.name AS mpa_name
                FROM films f
                JOIN films_likes l ON f.id = l.film_id
                JOIN mpa_ratings m ON f.mpa_rating = m.id
                WHERE l.user_id = ?
                  AND f.id NOT IN (SELECT film_id FROM films_likes WHERE user_id = ?)
                ORDER BY f.id
                """;

        String inClause = similarUserIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        sql = String.format(sql, inClause);

        List<Object> params = new ArrayList<>(similarUserIds);
        params.add(userId);
        log.info("Параметры для запроса рекомендаций: {}", params);
        return jdbcTemplate.query(sql, this::mapToFilm, params.toArray());
    }

    private List<Genre> getGenresByFilmId(Long filmId) {
        String sql = """
                SELECT g.id,
                g.name FROM
                genres g
                JOIN films_genres
                fg ON
                g.id =fg.genre_id
                WHERE fg.film_id =?
                ORDER BY
                g.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), filmId);
    }

    private Film mapToFilm(ResultSet rs, int rowNum) throws SQLException {
        int mpaId = rs.getInt("mpa_id");
        String mpaName = rs.getString("mpa_name");
        return Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration((long) rs.getInt("duration"))
                .mpa(new Mpa(mpaId, mpaName))
                .genres(getGenresByFilmId(rs.getLong("id")))
                .build();
    }
}
