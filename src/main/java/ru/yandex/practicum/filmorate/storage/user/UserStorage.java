package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getUsers();

    User createUser(User user);

    Optional<User> updateUser(User user);

    void deleteUser(User user);

    Optional<User> getUserById(long id);
}
