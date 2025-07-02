package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {
    Friendship getFriendship(long fromUserId, long toUserId);
    void sendFriendRequest(long fromUserId, long toUserId);
    void acceptFriendRequest(long fromUserId, long toUserId);
    void rejectFriendRequest(long fromUserId, long toUserId);
    void removeFriendship(long fromUserId, long toUserId);
    List<Long> getFriendRequestsUserIdsForUser(long userId);
    public List<User> getRequestedFriends(long userId);
    List<User> getFriends(long userId);
}