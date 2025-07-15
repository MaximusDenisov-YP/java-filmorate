package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {

    Collection<Film> getSortedFilmsByDirectorAndSortedParam(long id, String sortBy);

    Collection<Director> getAllDirectors();

    Optional<Director> getDirectorById(long id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(long id);

}
