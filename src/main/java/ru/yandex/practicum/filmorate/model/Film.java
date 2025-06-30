package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class Film {

    private long id;
    @NotBlank
    private String name;
    @Size(min = 1, max = 200, message = "Размер должен находиться в диапазоне от 1 до 200 символов")
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @Min(1)
    private Long duration;
    private Set<Long> usersLikes; // После проверки схемы DB - заменить обычным целочисленным значением, так как логика приложения будет изменена.
    private Genre genre;
    private MpaRating rating;

    enum Genre {
        COMEDY("Комедия"),
        DRAMA("Драма"),
        CARTOON("Мультфильм"),
        THRILLER("Триллер"),
        DOCUMENTARY("Документальный"),
        ACTION("Боевик");

        private final String localeName;

        Genre(String localeName) {
            this.localeName = localeName;
        }

        public String getLocaleName() {
            return localeName;
        }
    }

    enum MpaRating {
        G("Без возрастных ограничений"),
        PG("Рекомендуется просмотр с родителями"),
        PG_13("Детям до 13 лет нежелательно"),
        R("Только в сопровождении взрослых (до 17 лет)"),
        NC_17("Просмотр запрещён лицам до 18 лет");

        private final String description;

        MpaRating(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}