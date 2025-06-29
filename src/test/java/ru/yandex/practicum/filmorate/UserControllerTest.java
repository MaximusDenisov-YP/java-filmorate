package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private UserController userController;
    private InMemoryUserStorage inMemoryUserStorage;
    private UserService userService;

    @BeforeEach
    void setUp() {
        inMemoryUserStorage = new InMemoryUserStorage();
        userService = new UserService(inMemoryUserStorage);
        userController = new UserController(inMemoryUserStorage, userService);
    }

    @Test
    void createUserShouldSetNameToLoginIfNameIsBlank() {
        User user = User.builder()
                .email("test@example.com")
                .login("testuser")
                .name(" ")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userController.createUser(user);

        assertThat(createdUser.getName()).isEqualTo("testuser");
        assertThat(createdUser.getId()).isNotNull();
    }

    @Test
    void updateUserShouldUpdateExistingUser() {
        User user = User.builder()
                .email("email@example.com")
                .login("login")
                .name("Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User created = userController.createUser(user);

        created.setName("Updated Name");
        User updated = userController.updateUser(created);

        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateUserShouldThrowExceptionIfUserNotFound() {
        User user = User.builder()
                .email("email@example.com")
                .id(999)
                .login("login")
                .name("Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(NotFoundException.class, () -> userController.updateUser(user));
    }

    @Test
    void getUsersShouldReturnAllUsers() {
        User user1 = User.builder()
                .email("a@example.com")
                .login("a")
                .name("A")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userController.createUser(user1);

        User user2 = User.builder()
                .email("b@example.com")
                .login("b")
                .name("B")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();
        userController.createUser(user2);

        Collection<User> users = userController.getUsers();
        assertThat(users).hasSize(2);
    }
}
