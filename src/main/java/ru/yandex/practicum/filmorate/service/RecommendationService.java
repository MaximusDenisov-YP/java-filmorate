package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    private final RecommendationStorage recommendationStorage;
    private final UserService userService;

    public List<Film> getRecommendations(long userId) {
        log.info("Формирование рекомендаций для пользователя ID: {}", userId);

        try {
            userService.getUserById(userId);

            log.debug("Поиск рекомендаций для пользователя ID: {}", userId);

            if (!recommendationStorage.hasUserLikes(userId)) {
                log.debug("У пользователя {} нет лайков", userId);
                return Collections.emptyList();
            }

            List<Long> topSimilarUsers = recommendationStorage.findTopSimilarUsers(userId);
            if (topSimilarUsers.isEmpty()) {
                log.debug("Для пользователя {} не найдено похожих пользователей", userId);
                return Collections.emptyList();
            }

            log.debug("Найдено {} топ похожих пользователей для ID {}", topSimilarUsers.size(), userId);

            List<Film> recommendations = recommendationStorage.findRecommendedFilms(userId, topSimilarUsers);

            if (recommendations.isEmpty()) {
                log.info("Рекомендации для пользователя {} не найдены", userId);
            } else {
                log.info("Найдено {} рекомендаций для пользователя {}", recommendations.size(), userId);
            }

            return recommendations;

        } catch (EmptyResultDataAccessException e) {
            String message = String.format("Пользователь с ID %d не найден", userId);
            log.error(message);
            throw new NotFoundException(message);
        } catch (DataAccessException e) {
            String message = String.format("Ошибка при получении рекомендаций для пользователя %d", userId);
            log.error(message, e);
            throw new NotFoundException(message);
        }
    }
}