package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmService.class,
        FilmDbStorage.class,
        UserDbStorage.class,
        LikeDbStorage.class,
        MpaDbStorage.class,
        GenreDbStorage.class
})
public class FilmServiceIntegrationTest {

    private final FilmService filmService;

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
        List<Film> popularFilms = filmService.getPopularFilms(10);
        assertThat(popularFilms).isNotNull();
        assertThat(popularFilms.size()).isLessThanOrEqualTo(10);
    }
}