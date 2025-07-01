package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.friendship.InMemoryFriendshipStorage;
import ru.yandex.practicum.filmorate.storage.like.InMemoryLikeStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController filmController;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FriendshipStorage friendshipStorage;
    private LikeStorage likeStorage;
    private FilmService filmService;
    private UserService userService;


    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        likeStorage = new InMemoryLikeStorage();
        friendshipStorage = new InMemoryFriendshipStorage();
        userService = new UserService(userStorage, friendshipStorage);
        filmService = new FilmService(filmStorage, userStorage, likeStorage);
        filmController = new FilmController(filmStorage, filmService);
    }

    @Test
    void createFilmShouldAssignIdAndStoreFilm() {
        Film film = Film.builder()
                .name("Inception")
                .description("A mind-bending thriller")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148L)
                .build();

        Film created = filmController.createFilm(film);

        assertThat(created.getId()).isNotNull();
        assertThat(filmController.getFilms()).contains(created);
    }

    @Test
    void createFilmShouldThrowExceptionIfReleaseDateTooEarly() {
        Film film = Film.builder()
                .name("Early Film")
                .description("Old")
                .releaseDate(LocalDate.of(1800, 1, 1))
                .duration(120L)
                .build();

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void updateFilmShouldModifyExistingFilm() {
        Film film = Film.builder()
                .name("Original Title")
                .description("Original")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100L)
                .build();
        Film created = filmController.createFilm(film);

        Film updatedFilm = Film.builder()
                .id(created.getId())
                .name("Updated Title")
                .description("Updated")
                .releaseDate(LocalDate.of(2010, 5, 20))
                .duration(110L)
                .build();

        Film result = filmController.updateFilm(updatedFilm);

        assertThat(result.getName()).isEqualTo("Updated Title");
        assertThat(result.getDuration()).isEqualTo(110L);
    }

    @Test
    void updateFilmShouldThrowExceptionIfFilmNotFound() {
        Film film = Film.builder()
                .id(999)
                .name("Non-existing")
                .description("None")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100L)
                .build();

        assertThrows(NotFoundException.class, () -> filmController.updateFilm(film));
    }

    @Test
    void getFilmsShouldReturnAllStoredFilms() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Desc 1")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(90L)
                .build();
        Film film2 = Film.builder()
                .name("Film 2")
                .description("Desc 2")
                .releaseDate(LocalDate.of(2002, 2, 2))
                .duration(120L)
                .build();

        filmController.createFilm(film1);
        filmController.createFilm(film2);

        Collection<Film> films = filmController.getFilms();
        assertThat(films).hasSize(2);
    }
}
