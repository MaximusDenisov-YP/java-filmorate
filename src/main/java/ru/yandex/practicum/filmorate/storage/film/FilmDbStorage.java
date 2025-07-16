package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;


@Repository
@Slf4j
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String GET_COMMON_FILMS_SQL =
            """
                    WITH common_films AS (
                        SELECT fl.film_id
                        FROM films_likes fl
                        WHERE fl.user_id IN (?, ?)
                        GROUP BY fl.film_id
                        HAVING COUNT(DISTINCT fl.user_id) = 2
                    ),
                    film_likes AS (
                        SELECT film_id, COUNT(*) AS likes_count
                        FROM films_likes
                        GROUP BY film_id
                    )
                    SELECT
                        f.id,
                        f.name,
                        f.description,
                        f.release_date,
                        f.duration,
                        f.mpa_rating AS mpa_id,
                        mr.name AS mpa_name,
                        fl.likes_count
                    FROM films f
                    JOIN mpa_ratings mr ON mr.id = f.mpa_rating
                    JOIN film_likes fl ON fl.film_id = f.id
                    WHERE f.id IN (SELECT film_id FROM common_films)
                    ORDER BY fl.likes_count DESC, f.release_date DESC
                    """;

    @Override
    public List<Film> getFilms() {
        String sql = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs));
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        String sql = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
                LEFT JOIN reviews r ON r.FILM_ID = f.id
                WHERE f.id = ?
                """;
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(films.get(0));
    }

    @Override
    public Film createFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        insertFilmGenres(film.getId(), film.getGenres());
        insertFilmDirectors(film.getId(), film.getDirectors());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_rating = ? WHERE id = ?";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbcTemplate.update("DELETE FROM films_genres WHERE film_id = ?", film.getId());
        insertFilmGenres(film.getId(), film.getGenres());

        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
        insertFilmDirectors(film.getId(), film.getDirectors());

        return film;
    }


    private void insertFilmDirectors(Long filmId, List<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, directors.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return directors.size();
            }
        });
    }

    @Transactional
    @Override
    public void deleteFilm(Long id) {
        String getReviewsIds = "SELECT id FROM reviews WHERE film_id = ?";
        List<Long> reviewsIds = jdbcTemplate.query(getReviewsIds,
                (rs, rowNum) -> rs.getLong("id"), id);
        String stringReviewsIds = reviewsIds.stream()
                .map(reviewId -> "?")
                .collect(Collectors.joining(","));

        String deleteReviewsLikes = "DELETE FROM reviews_likes WHERE review_id IN (" + stringReviewsIds + ")";
        String deleteReviews = "DELETE FROM reviews WHERE film_id = ?";
        String deleteFilmGenres = "DELETE FROM films_genres WHERE film_id = ?";
        String deleteFilmsLikes = "DELETE FROM films_likes WHERE film_id = ?";
        String deleteFilms = "DELETE FROM films WHERE id = ?";

        jdbcTemplate.update(deleteReviewsLikes);
        jdbcTemplate.update(deleteReviews, id);
        jdbcTemplate.update(deleteFilmGenres, id);
        jdbcTemplate.update(deleteFilmsLikes, id);
        jdbcTemplate.update(deleteFilms, id);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name, COUNT(l.user_id) AS likes_count
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
                LEFT JOIN films_likes l ON f.id = l.film_id
                GROUP BY f.id, m.id, m.name
                ORDER BY likes_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), count);
    }

    @Override
    public List<Film> getPopularFilmsByGenreAndYear(Integer count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
                    SELECT f.*,
                           m.id   AS mpa_id,
                           m.name AS mpa_name,
                           COUNT(l.user_id) AS likes_count
                    FROM films f
                    LEFT JOIN mpa_ratings  m ON f.mpa_rating = m.id
                    LEFT JOIN films_likes  l ON f.id = l.film_id
                    LEFT JOIN films_genres g ON f.id = g.film_id
                """);

        List<Object> args = new ArrayList<>();


        if (genreId != null) {
            sql = new StringBuilder(sql.toString().replace(
                    "LEFT JOIN films_genres g ON f.id = g.film_id",
                    "LEFT JOIN films_genres g ON f.id = g.film_id WHERE g.genre_id = ?"
            ));
            args.add(genreId);
        }

        if (year != null && genreId != null) {
            sql.append(" AND EXTRACT(YEAR FROM f.release_date) = ?");
            args.add(year);
        } else if (year != null) {
            sql.append(" WHERE EXTRACT(YEAR FROM f.release_date) = ?");
            args.add(year);
        }

        sql.append("""
                \nGROUP BY f.id, m.id, m.name
                ORDER BY likes_count DESC
                """
        );

        if (count != null) {
            sql.append("""
                    LIMIT ?
                    """);
            args.add(count);
        }
        log.debug("getPopularFilmsByGenreAndYear.class sql = \n{}\nargs = {}", sql, args);
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapRowToFilm(rs), args.toArray());
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));

        int mpaId = rs.getInt("mpa_id");
        String mpaName = rs.getString("mpa_name");
        if (mpaId != 0 && mpaName != null) {
            film.setMpa(new Mpa(mpaId, mpaName));
        }

        film.setGenres(getGenresByFilmId(film.getId()));
        film.setDirectors(getDirectorsByFilmId(film.getId()));
        return film;
    }

    private void insertFilmGenres(Long filmId, List<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        Set<Integer> uniqueGenreIds = new HashSet<>();
        List<Genre> filteredGenres = genres.stream()
                .filter(genre -> uniqueGenreIds.add(genre.getId()))
                .toList();

        String sql = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setInt(2, filteredGenres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return filteredGenres.size();
            }
        });
    }

    private List<Director> getDirectorsByFilmId(Long filmId) {
        String sql = """
                SELECT d.id, d.name FROM directors d
                JOIN film_directors fd ON d.id = fd.director_id
                WHERE fd.film_id = ?
                ORDER BY d.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Director(rs.getLong("id"), rs.getString("name")), filmId);
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

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return jdbcTemplate.query(
                GET_COMMON_FILMS_SQL,
                (rs, rowNum) -> {
                    Film film = mapRowToFilm(rs);
                    return film;
                },
                userId,
                friendId
        );
    }

    @Override
    public List<Film> searchFilmsByTitle(String query) {
        String sql = """
            SELECT f.*, m.id as mpa_id, m.name as mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
            WHERE LOWER(f.name) LIKE ?
            """;
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), "%" + query.toLowerCase() + "%");
        films.forEach(this::loadFilmLikes);
        return films;
    }

    @Override
    public List<Film> searchFilmsByDirector(String query) {
        String sql = """
            SELECT f.*, m.id as mpa_id, m.name as mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
            LEFT JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            WHERE LOWER(d.name) LIKE ?
            """;
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), "%" + query.toLowerCase() + "%");
        films.forEach(this::loadFilmLikes);
        return films;
    }

    @Override
    public List<Film> searchFilmsByTitleAndDirector(String query) {
        String sql = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name,
                       SUM(CASE WHEN LOWER(f.name) LIKE ? THEN 1 ELSE 0 END) as title_matches,
                       SUM(CASE WHEN LOWER(d.name) LIKE ? THEN 1 ELSE 0 END) as director_matches
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
                LEFT JOIN film_directors fd ON f.id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.id
                WHERE LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ?
                GROUP BY f.id, m.id, m.name
                ORDER BY (title_matches + director_matches) DESC,
                         (SELECT COUNT(*) FROM films_likes WHERE film_id = f.id) DESC
                """;
        String searchPattern = "%" + query.toLowerCase() + "%";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs),
                searchPattern, searchPattern, searchPattern, searchPattern);
        films.forEach(this::loadFilmLikes);
        return films;
    }

    private void loadFilmLikes(Film film) {
        String sql = "SELECT user_id FROM films_likes WHERE film_id = ?";
        Set<Long> likes = new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, film.getId()));
        film.setUsersLikes(likes);
    }
}
