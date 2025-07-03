package ru.yandex.practicum.filmorate.storage.friendship;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

@Repository
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;

    public FriendshipDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Friendship getFriendship(long fromUserId, long toUserId) {
        String sql = """
                SELECT * FROM FRIENDSHIP
                WHERE (user_id_from = ? AND user_id_to = ?)
                   OR (user_id_from = ? AND user_id_to = ?)
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToFriendship(rs), fromUserId, toUserId);
    }

    @Override
    public void sendFriendRequest(long fromUserId, long toUserId) {
        String sql = """
                INSERT INTO friendship (user_id_from, user_id_to, friend_status)
                VALUES (?, ?, 'REQUESTED')
                """;
        jdbcTemplate.update(sql, fromUserId, toUserId);
    }

    @Override
    public void acceptFriendRequest(long fromUserId, long toUserId) {
        String sql = """
                UPDATE friendship
                SET friend_status = 'ACCEPTED'
                WHERE user_id_from = ? AND user_id_to = ?
                """;
        jdbcTemplate.update(sql, fromUserId, toUserId);
    }

    @Override
    public void rejectFriendRequest(long fromUserId, long toUserId) {
        String sql = """
                UPDATE friendship
                SET friend_status = 'REJECTED'
                WHERE user_id_from = ? AND user_id_to = ?
                """;
        jdbcTemplate.update(sql, fromUserId, toUserId);
    }

    @Override
    public void removeFriendship(long fromUserId, long toUserId) {
        String sql = """
                DELETE FROM friendship
                WHERE (user_id_from = ? AND user_id_to = ?)
                   OR (user_id_from = ? AND user_id_to = ?)
                """;
        jdbcTemplate.update(sql, fromUserId, toUserId, toUserId, fromUserId);
    }

    @Override
    public List<User> getFriends(long userId) {
        String sql = """
                SELECT u.* FROM users u
                JOIN friendship f ON (
                    (f.user_id_from = ? AND f.user_id_to = u.id AND f.friend_status IN ('ACCEPTED', 'REQUESTED'))
                    OR
                    (f.user_id_to = ? AND f.user_id_from = u.id AND f.friend_status = 'ACCEPTED')
                )
                """;

        return jdbcTemplate.query(sql, this::mapRowToUser, userId, userId);
    }

    @Override
    public List<User> getRequestedFriends(long userId) {
        String sql = """
                SELECT u.* FROM users u
                JOIN friendship f ON (
                    (f.user_id_from = ? AND f.user_id_to = u.id AND f.friend_status IN ('ACCEPTED', 'REQUESTED'))
                    OR
                    (f.user_id_to = ? AND f.user_id_from = u.id AND f.friend_status = 'ACCEPTED')
                )
                """;

        return jdbcTemplate.query(sql, this::mapRowToUser, userId, userId);
    }

    @Override
    public List<Long> getFriendRequestsUserIdsForUser(long userId) {
        String sql = """
                SELECT user_id_from FROM friendship
                WHERE user_id_to = ? AND friend_status = 'REQUESTED'
                """;
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    private Friendship mapRowToFriendship(ResultSet rs) throws SQLException {
        Friendship friendship = new Friendship();
        friendship.setFriendshipId(rs.getLong("friendship_id"));
        friendship.setUserIdFrom(rs.getLong("user_id_from"));
        friendship.setUserIdTo(rs.getLong("user_id_to"));
        friendship.setFriendStatus(Friendship.FriendStatus.valueOf(rs.getString("friend_status")));
        friendship.setCreatedAt(rs.getDate("created_at").toLocalDate());
        return friendship;
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .friends(new HashSet<>()) // по умолчанию пусто — заполняется отдельно
                .build();
    }
}
