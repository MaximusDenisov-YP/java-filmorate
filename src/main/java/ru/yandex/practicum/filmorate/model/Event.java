package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Positive
    private long eventId;
    @Positive
    private long userId;
    @NotNull
    private EventType eventType;
    @NotNull
    private Operation operation;
    @Positive
    @NotNull
    private long entityId;
    @NotNull
    private long timestamp;

    public Event(long userId, EventType eventType, Operation operation, long entityId) {
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
        this.timestamp = System.currentTimeMillis();
    }

    public enum EventType {
        LIKE,
        REVIEW,
        FRIEND
    }

    public enum Operation {
        REMOVE,
        ADD,
        UPDATE
    }
}
