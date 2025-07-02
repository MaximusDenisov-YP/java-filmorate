package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public class InMemoryGenreStorage implements GenreStorage {

    @Override
    public Optional<Genre> getById(int id) {
        return Optional.empty();
    }

    @Override
    public List<Genre> getAll() {
        return null;
    }
}
