package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private Set<Long> usersLikes;
    private List<Genre> genres;
    private Mpa mpa;

    @JsonProperty("directors")
    private List<Director> directors;

}