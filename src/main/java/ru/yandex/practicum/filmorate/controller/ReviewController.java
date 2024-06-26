package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.model.Constants.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@RequestBody @Valid Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody @Valid Review review) {
        return reviewService.update(review);
    }

    @DeleteMapping("/{reviewId}")
    public Map<String, String> removeReview(@PathVariable Integer reviewId) {
        return reviewService.removeReview(reviewId);
    }

    @GetMapping("/{reviewId}")
    public Review getReview(@PathVariable Integer reviewId) {
        return reviewService.getReview(reviewId);
    }

    @GetMapping
    public List<Review> findAll(
            @RequestParam(value = "filmId", defaultValue = "0") Integer filmId,
            @RequestParam(value = "count", defaultValue = "10") Integer count) {
        return reviewService.findAll(filmId, count);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public Map<String, String> addOpinionPositive(@PathVariable Integer reviewId,
                                                  @PathVariable Integer userId) {
        return reviewService.addOpinion(reviewId, userId, GRADE_POSITIVE);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public Map<String, String> addOpinionNegative(@PathVariable Integer reviewId,
                                                  @PathVariable Integer userId) {
        return reviewService.addOpinion(reviewId, userId, GRADE_NEGATIVE);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public Map<String, String> removeOpinionPositive(@PathVariable Integer reviewId,
                                                  @PathVariable Integer userId) {
        return reviewService.removeOpinion(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public Map<String, String> removeOpinionNegative(@PathVariable Integer reviewId,
                                                     @PathVariable Integer userId) {
        return reviewService.removeOpinion(reviewId, userId);
    }
}
