package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Map;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    Map<String, String> removeReview(int reviewId);

    Review getReview(int reviewId);

    List<Review> findAll(int id, int count);

    Map<String, String> addOpinion(int reviewId, int userId, int value);

    Map<String, String> removeOpinion(int reviewId, int userId);

}
