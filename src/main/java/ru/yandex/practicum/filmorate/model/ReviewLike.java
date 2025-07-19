package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewLike {
    private Long id;
    private Long reviewId;
    private Long userId;
    private Boolean isPositive;
}