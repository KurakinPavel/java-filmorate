package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Map;

public interface ReviewStorage {

    int GRADE_ID_POSITIVE = 1;
    int GRADE_ID_NEGATIVE = 0;

    Review create(Review review);

    Review update(Review review);

    Map<String, String> removeReview(int reviewId);

    Review getReview(int reviewId);

    List<Review> findAll(int id, int count);

    Map<String, String> addOpinionPositive(int reviewId, int userId);

    Map<String, String> addOpinionNegative(int reviewId, int userId);

    Map<String, String> removeOpinionPositive(int reviewId, int userId);

    Map<String, String> removeOpinionNegative(int reviewId, int userId);

}
