package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        User result = userStorage.createUser(user);
        log.info("Создан пользователь {}", result);
        return result;
    }

    public User updateUser(User user) {
        getUserById(user.getId());
        User result = userStorage.updateUser(user)
                .orElseThrow(() -> new NotFoundException("Не удалось обновить пользователя"));
        log.info("Обновлён пользователь {}", result);
        return result;
    }

    public void deleteUser(Long id) {
        if (userStorage.getUserById(id).isEmpty()) {
            throw new NotFoundException("Пользователь для удаления не найден");
        }
        userStorage.deleteUser(id);
        log.info("Удалён пользователь с ID {}", id);
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public List<User> getFriends(long id) {
        getUserById(id);
        return friendshipStorage.getFriends(id);
    }

    public List<User> getCommonFriends(long id, long friendId) {
        List<User> firstUserFriends = friendshipStorage.getFriends(id);
        List<User> secondUserFriends = friendshipStorage.getFriends(friendId);
        return firstUserFriends.stream().filter(secondUserFriends::contains).toList();
    }

    public void sendFriendRequest(long fromUserId, long toUserId) {
        getUserById(fromUserId);
        getUserById(toUserId);
        if (friendshipStorage.getFriendRequestsUserIdsForUser(toUserId).contains(fromUserId)) {
            log.info("ОТПРАВЛЕН ЗАПРОС ОТ ID {} к ID {}", fromUserId, toUserId);
            log.info("ДРУЖБА АВТОМАТИЧЕСКИ ПРИНЯТА ОТ ID {} к ID {}", toUserId, fromUserId);
            friendshipStorage.acceptFriendRequest(toUserId, fromUserId);
            return;
        }
        log.info("ОТПРАВЛЕН ЗАПРОС ОТ ID {} к ID {}", fromUserId, toUserId);
        friendshipStorage.sendFriendRequest(fromUserId, toUserId);
    }

    public void removeFriend(long fromUserId, long toUserId) {
        getUserById(fromUserId);
        getUserById(toUserId);
        log.info("ОТПРАВЛЕН ЗАПРОС НА УДАЛЕНИЕ ID {} к ID {}", fromUserId, toUserId);
        List<User> friendIdsFrom = friendshipStorage.getFriends(fromUserId);
        if (friendIdsFrom != null && friendIdsFrom.stream().map(User::getId).anyMatch(id -> id == toUserId)) {
            friendshipStorage.removeFriendship(fromUserId, toUserId);
        }
    }

}
