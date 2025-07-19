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
                SELECT f.*, m.id as mpa_id, m.name as mpa_name,
                d.id AS director_id, d.name AS director_name,
                g.id AS genre_id, g.name AS genre_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
                LEFT JOIN film_directors fd ON f.id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.id
                LEFT JOIN films_genres fg ON f.id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.id
                WHERE fd.director_id = ?
                ORDER BY
                    CASE WHEN ? = 'likes' THEN (SELECT COUNT(*) FROM films_likes WHERE film_id = f.id) END DESC,
                    CASE WHEN ? = 'year' THEN f.release_date END
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), id, sortBy, sortBy);
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

        long directorId = rs.getLong("director_id");
        String directorName = rs.getString("director_name");
        if (directorId != 0 && directorName != null) {
            film.setDirectors(List.of(new Director(
                    directorId,
                    directorName
            )));
        }

        int genreId = rs.getInt("genre_id");
        String genreName = rs.getString("genre_name");
        if (genreId != 0 && mpaName != null) {
            film.setGenres(List.of(new Genre(
                    genreId,
                    genreName
            )));
        }
        return film;
    }
}