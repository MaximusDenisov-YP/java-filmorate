package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь создан! " + user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        validateUser(user);
        User currentUser = users.get(user.getId());
        if (currentUser == null) {
            log.warn("Обновление не выполнено — пользователь с ID={} не найден", user.getId());
            throw new ValidationException("Пользователя с таким ID не существует!");
        }
        currentUser.setName(user.getName());
        currentUser.setEmail(user.getEmail());
        currentUser.setBirthday(user.getBirthday());
        currentUser.setLogin(user.getLogin());
        log.info("Пользователь с ID={} обновлён: {}", currentUser.getId(), currentUser);
        return currentUser;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Почта должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank())
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");

        if (user.getBirthday().isAfter(LocalDate.now()))
            throw new ValidationException("Дата рождения не может быть установлена в будущем времени");
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
