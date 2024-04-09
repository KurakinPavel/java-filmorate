package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class Review {
    protected int reviewId;
    @NotBlank
    @Size(min = 1, max = 500)
    protected String content;
    @NotNull
    protected Boolean isPositive;
    protected int userId;
    protected int filmId;
    protected int useful;

    public Review(int reviewId, String content, Boolean isPositive, int userId, int filmId, int useful) {
        this.reviewId = reviewId;
        this.content = content;
        this.isPositive = isPositive;
        this.userId = validUserId(userId);
        this.filmId = validFilmId(filmId);
        this.useful = useful;
    }

    private int validUserId(int userId) {
        if (userId == 0) throw new IllegalArgumentException("Пользователь с id " + userId + " не существует");
        return userId;
    }

    private int validFilmId(int filmId) {
        if (filmId == 0) throw new IllegalArgumentException("Фильм с id " + filmId + " не существует");
        return filmId;
    }

    public Map<String, Object> reviewToMap(int directionIdFromDB) {
        Map<String, Object> values = new HashMap<>();
        values.put("CONTENT", content);
        values.put("USER_ID", userId);
        values.put("FILM_ID", filmId);
        values.put("DIRECTION_ID", directionIdFromDB);
        return values;
    }
}
