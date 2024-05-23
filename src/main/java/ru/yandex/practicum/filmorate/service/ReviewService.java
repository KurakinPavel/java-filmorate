package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(Review review) {
        validUserAndFilm(review);
        return reviewStorage.create(review);
    }

    private void validUserAndFilm(Review review) {
        userStorage.getUser(review.getUserId());
        filmStorage.getFilm(review.getFilmId());
    }

    public Review update(Review review) {
        return reviewStorage.update(review);
    }

    public Map<String, String> removeReview(int reviewId) {
        return reviewStorage.removeReview(reviewId);
    }

    public Review getReview(int reviewId) {
        return reviewStorage.getReview(reviewId);
    }

    public List<Review> findAll(int id, int count) {
        return reviewStorage.findAll(id, count);
    }

    public Map<String, String> addOpinion(int reviewId, int userId, int value) {
        return reviewStorage.addOpinion(reviewId, userId, value);
    }

    public Map<String, String> removeOpinion(int reviewId, int userId) {
        return reviewStorage.removeOpinion(reviewId, userId);
    }
}
