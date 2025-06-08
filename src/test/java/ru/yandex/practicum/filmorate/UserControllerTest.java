package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
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
    void createUserShouldThrowExceptionIfEmailInvalid() {
        User user = User.builder()
                .email("invalid-email")
                .login("user")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.createUser(user));
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

        assertThrows(ValidationException.class, () -> userController.updateUser(user));
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
