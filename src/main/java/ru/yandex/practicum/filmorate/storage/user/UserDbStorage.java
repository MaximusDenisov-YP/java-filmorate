package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Repository
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> getUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = mapRowToUser(rs, rowNum);
            user.setFriends(getFriendIds(user.getId()));
            return user;
        });
    }

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        user.setFriends(new HashSet<>());
        return user;
    }

    @Override
    public Optional<User> updateUser(User user) {
        hasUser(user.getId());
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return Optional.of(user);
    }

    @Override
    public void deleteUser(User user) {
        hasUser(user.getId());
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, user.getId());
    }

    @Override
    public Optional<User> getUserById(long id) {
        hasUser(id);
        String sql = "SELECT * FROM users WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToUser, id));
    }

    private Set<Long> getFriendIds(long userId) {
        String sql = """
                SELECT user_id_to FROM friendship
                WHERE user_id_from = ? AND friend_status = 'ACCEPTED'
                UNION
                SELECT user_id_from FROM friendship
                WHERE user_id_to = ? AND friend_status = 'ACCEPTED'
                """;
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId, userId));
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    public boolean hasUser(Long userId) {
        if (!getUsers().stream().map(User::getId).toList().contains(userId)) {
            log.warn("Операция не выполнена — пользователь с ID={} не найден", userId);
            throw new NotFoundException("Пользователь с указанным ID - не найден");
        }
        return true;
    }
}
