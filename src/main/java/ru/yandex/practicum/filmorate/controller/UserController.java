package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final UserService userService;

    public UserController(@Qualifier("userDbStorage") UserStorage userStorage, FriendshipStorage friendshipStorage, UserService userService) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userStorage.createUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userStorage.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@Positive @PathVariable long id, @Positive @PathVariable long friendId) {
        userService.sendFriendRequest(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@Positive @PathVariable long id, @Positive @PathVariable long friendId) {
        userService.removeFriend(id, friendId);
    }

    // TODO: Проверить, задублировал логику добавления в друзья в UserStorage и FriendshipStorage
    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@Positive @PathVariable long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@Positive @PathVariable long id, @Positive @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}