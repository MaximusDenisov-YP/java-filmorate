package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public class InMemoryFriendshipStorage implements FriendshipStorage {
    @Override
    public Friendship getFriendship(long fromUserId, long toUserId) {
        return null;
    }

    @Override
    public void sendFriendRequest(long fromUserId, long toUserId) {

    }

    @Override
    public void acceptFriendRequest(long fromUserId, long toUserId) {

    }

    @Override
    public void rejectFriendRequest(long fromUserId, long toUserId) {

    }

    @Override
    public void removeFriendship(long fromUserId, long toUserId) {

    }

    @Override
    public List<Long> getFriendRequestsUserIdsForUser(long userId) {
        return null;
    }

    @Override
    public List<User> getFriends(long userId) {
        return null;
    }
}
