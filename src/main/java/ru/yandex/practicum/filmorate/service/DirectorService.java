package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundExceptionDirectors;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@Slf4j
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(@Qualifier("directorDbStorage") DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director getDirectorById(long id) {
        return directorStorage.getDirectorById(id).orElseThrow(() ->
                new NotFoundExceptionDirectors("Режиссер не найден: "));
    }

    public Director createDirector(Director director) {
        validateDirector(director);
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        validateDirector(director);
        getDirectorById(director.getId());
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(long id) {
        directorStorage.deleteDirector(id);
    }

    public Collection<Film> getSortedFilmsByDirector(long directorId, String sortBy) {
        return directorStorage.getSortedFilmsByDirectorAndSortedParam(directorId, sortBy);
    }

    private void validateDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режисера не может быть пустым");
        }
    }
}