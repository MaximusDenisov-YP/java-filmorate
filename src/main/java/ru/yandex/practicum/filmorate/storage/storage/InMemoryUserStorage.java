package ru.yandex.practicum.filmorate.storage.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User createUser(User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        user.setFriends(new HashSet<>());
        users.put(user.getId(), user);
        log.info("Пользователь создан! " + user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        validateUser(user);
        User currentUser = users.get(user.getId());
        if (currentUser == null) {
            log.warn("Обновление не выполнено — пользователь с ID={} не найден", user.getId());
            throw new NotFoundException("Пользователя с таким ID не существует!");
        }
        currentUser.setName(user.getName());
        currentUser.setEmail(user.getEmail());
        currentUser.setBirthday(user.getBirthday());
        currentUser.setLogin(user.getLogin());
        log.info("Пользователь с ID={} обновлён: {}", currentUser.getId(), currentUser);
        return currentUser;
    }

    public User getUserById(long id) {
        if (users.get(id) == null) {
            throw new NotFoundException(String.format("Пользователя с ID %d - не существует!", id));
        }
        return users.get(id);
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

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
