package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public List<User> getFriends(long id) {
        return friendshipStorage.getFriends(id);
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

    public void sendFriendRequest(long fromUserId, long toUserId) {
//        if (userToUpdate.getFriends().contains(toUserId)) {
//            throw new FriendshipException("Пользователь уже находится в списке друзей пользователя-отправителя");
//        }
        if (friendshipStorage.getFriendRequestsUserIdsForUser(toUserId).contains(fromUserId)) {
            friendshipStorage.acceptFriendRequest(toUserId, fromUserId);
        }
        log.info("ОТПРАВЛЕН ЗАПРОС ОТ ID {} к ID {}", fromUserId, toUserId);
        friendshipStorage.sendFriendRequest(fromUserId, toUserId);
    }

    public void removeFriend(long fromUserId, long toUserId) {
        friendshipStorage.removeFriendship(fromUserId, toUserId);
        User firstUser = userStorage.getUserById(fromUserId);
        User secondUser = userStorage.getUserById(toUserId);
        firstUser.getFriends().remove(toUserId);
        secondUser.getFriends().remove(fromUserId);
        userStorage.updateUser(firstUser);
        userStorage.updateUser(secondUser);
    }

    public void acceptFriendRequest(long fromUserId, long toUserId) {
        friendshipStorage.acceptFriendRequest(fromUserId, toUserId);
    }

    public void rejectFriendRequest(long fromUserId, long toUserId) {
        friendshipStorage.rejectFriendRequest(fromUserId, toUserId);
        User userToUpdate = userStorage.getUserById(toUserId);
        userToUpdate.getFriends().remove(fromUserId);
        userStorage.updateUser(userToUpdate);
    }
}
