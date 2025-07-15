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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
@Slf4j
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

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
                WHERE f.id = ?
                """;
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToFilm(rs), id));
    }

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

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
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
                ps.setLong(2, directors.get(i).getId()); // Изменено с setInt на setLong
            }

            @Override
            public int getBatchSize() {
                return directors.size();
            }
        });
    }


    @Override
    public void deleteFilm(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name, COUNT(l.user_id) AS likes_count
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY f.id, m.id, m.name
                ORDER BY likes_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), count);
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

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

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
                    SELECT g.id, g.name FROM genres g
                    JOIN film_genres fg ON g.id = fg.genre_id
                    WHERE fg.film_id = ?
                    ORDER BY g.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), filmId);
    }
}
