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

    private Long friendshipId; // Уникальный идентификатор дружбы
    @NotNull
    private Long userIdFrom; // Пользователь, отправивший запрос
    @NotNull
    private Long userIdTo; // Пользователь, которому отправлен запрос
    @NotNull
    private FriendStatus friendStatus; // Статус дружбы
    private LocalDate createdAt; // Дата создания связи

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