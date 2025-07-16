package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Repository
@Slf4j
@RequiredArgsConstructor
@Qualifier("directorDbStorage")
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> getSortedFilmsByDirectorAndSortedParam(long id, String sortBy) {
        String sql = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
                LEFT JOIN film_directors fd ON f.id = fd.film_id
                WHERE fd.director_id = ?
                ORDER BY
                    CASE WHEN ? = 'likes' THEN (SELECT COUNT(*) FROM films_likes WHERE film_id = f.id) END DESC,
                    CASE WHEN ? = 'year' THEN f.release_date END ASC
                """;

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), id, sortBy, sortBy);

        films.forEach(film -> {
            List<Director> directors = getDirectorsByFilmId(film.getId());
            film.setDirectors(directors);

            List<Genre> genres = getGenresByFilmId(film.getId());
            film.setGenres(genres);
        });

        return films;
    }

    private List<Director> getDirectorsByFilmId(Long filmId) {
        String sql = """
                SELECT d.id, d.name
                FROM directors d
                JOIN film_directors fd ON d.id = fd.director_id
                WHERE fd.film_id = ?
                ORDER BY d.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Director(rs.getLong("id"), rs.getString("name")),
                filmId);
    }

    @Override
    public Collection<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Director(
                rs.getLong("id"),
                rs.getString("name")
        ));
    }

    @Override
    public Optional<Director> getDirectorById(long id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Director(
                    rs.getLong("id"),
                    rs.getString("name")
            ), id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Director createDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKey().longValue());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, director.getName(), director.getId());
        return director;
    }

    @Override
    public void deleteDirector(long id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private List<Genre> getGenresByFilmId(long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN films_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")
        ), filmId);
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

        film.setDirectors(new ArrayList<>());
        film.setGenres(new ArrayList<>());

        return film;
    }

}
