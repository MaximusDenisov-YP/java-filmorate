package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {

    private Long friendshipId;
    @NotNull
    private Long userIdFrom;
    @NotNull
    private Long userIdTo;
    @NotNull
    private FriendStatus friendStatus;
    private LocalDate createdAt;

    public enum FriendStatus {
        REQUESTED("Неподтвержденная дружба"),
        ACCEPTED("Подтвержденная дружба");

        private final String description;

        FriendStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}