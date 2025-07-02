package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaRatingController {
    private final FilmService filmService;

    @GetMapping
    public List<Mpa> getAllRatings() {
        return filmService.getAllRatings();
    }

    @GetMapping("/{id}")
    public Mpa getRatingById(@PathVariable @Positive int id) {
        return filmService.getRatingById(id);
    }
}