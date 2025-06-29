package ru.yandex.practicum.filmorate.storage.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> getUsers();

    User createUser(User user);

    User updateUser(User user);

    User getUserById(long id);
}
