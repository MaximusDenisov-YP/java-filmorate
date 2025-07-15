package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmLikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewLikeDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
public class FilmServiceIntegrationTest {
    @Autowired
    private FilmService filmService;

    @Test
    public void testCreateAndGetFilmById() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Dreams within dreams");
        film.setDuration(148L);
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setMpa(new Mpa(1, null));

        Film createdFilm = filmService.createFilm(film);
        Film foundFilm = filmService.getFilmById(createdFilm.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getName()).isEqualTo("Inception");
        assertThat(foundFilm.getMpa()).isNotNull();
        assertThat(foundFilm.getMpa().getId()).isEqualTo(1);
    }

    @Test
    public void testGetPopularFilms() {
        List<Film> popularFilms = filmService.getPopularFilmsFiltered(10, null, null);
        assertThat(popularFilms).isNotNull();
        assertThat(popularFilms.size()).isLessThanOrEqualTo(10);
    }
}