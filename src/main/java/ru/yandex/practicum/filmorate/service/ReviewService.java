package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public Review create(Review review) {
        return reviewStorage.create(review);
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

    public Map<String, String> addOpinionPositive(int reviewId, int userId) {
        return reviewStorage.addOpinionPositive(reviewId, userId);
    }

    public Map<String, String> addOpinionNegative(int reviewId, int userId) {
        return reviewStorage.addOpinionNegative(reviewId, userId);
    }

    public Map<String, String> removeOpinionPositive(int reviewId, int userId) {
        return reviewStorage.removeOpinionPositive(reviewId, userId);
    }

    public Map<String, String> removeOpinionNegative(int reviewId, int userId) {
        return reviewStorage.removeOpinionNegative(reviewId, userId);
    }
}
