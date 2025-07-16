package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.*;
import java.util.List;
import java.util.Objects;

@Repository
@Slf4j
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Event createEvent(Event event) {
        String sql = "INSERT INTO events (user_id, event_type, operation, entity_id, timestamp) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, event.getUserId());
            ps.setString(2, event.getEventType().toString());
            ps.setString(3, event.getOperation().toString());
            ps.setLong(4, event.getEntityId());
            ps.setLong(5, event.getTimestamp());
            return ps;
        }, keyHolder);

        event.setEventId((Objects.requireNonNull(keyHolder.getKey()).longValue()));
        return event;
    }

    @Override
    public List<Event> getEvents(long id) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToEvent(rs), id);
    }

    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("id"));
        event.setUserId(rs.getLong("user_id"));
        event.setEventType(Event.EventType.valueOf(rs.getString("event_type").toUpperCase()));
        event.setOperation(Event.Operation.valueOf(rs.getString("operation").toUpperCase()));
        event.setEntityId(rs.getLong("entity_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        return event;
    }
}
