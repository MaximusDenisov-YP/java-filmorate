package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getFriends(long id) {
//        checkUser(id, "Указанный пользователь - не существует!");
        Set<Long> friendsIds = userStorage.getUserById(id).getFriends();
        return userStorage.getUsers()
                .stream()
                .filter(user -> friendsIds.contains(user.getId()))
                .toList();
    }

    public List<User> getCommonFriends(long id, long friendId) {
        Set<Long> currentUsersFriends = userStorage.getUserById(id).getFriends();
        Set<Long> commonUsersFriends = userStorage.getUserById(friendId)
                .getFriends()
                .stream()
                .filter(currentUsersFriends::contains)
                .collect(Collectors.toSet());
        return userStorage.getUsers().stream()
                .filter(user -> commonUsersFriends.contains(user.getId()))
                .toList();
    }

    public void addFriend(long id, long friendId) {
        userStorage.getUserById(id).getFriends().add(friendId);
        userStorage.getUserById(friendId).getFriends().add(id);
    }

    public void removeFriend(long id, long friendId) {
        userStorage.getUserById(id).getFriends().remove(friendId);
        userStorage.getUserById(friendId).getFriends().remove(id);
    }
}
