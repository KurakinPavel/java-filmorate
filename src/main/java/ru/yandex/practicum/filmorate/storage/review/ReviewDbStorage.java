package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.*;

@Slf4j
@Component
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review create(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("REVIEWS")
                .usingGeneratedKeyColumns("REVIEW_ID");
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(review.reviewToMap()).intValue());
        log.info("Добавлен новый отзыв с id {}", review.getReviewId());
        return review;
    }

    @Override
    public Review update(Review review) {
        if (review.getReviewId() == 0) {
            create(review);
        } else {
            String sqlQuery = "UPDATE REVIEWS SET " +
                    "CONTENT = ?, DIRECTION_ID = ? WHERE REVIEW_ID = ?";
            int linesChanged = jdbcTemplate.update(sqlQuery,
                    review.getContent(),
                    review.setDirectionId(review.getIsPositive()),
                    review.getReviewId());
            if (linesChanged > 0) {
                log.info("Обновлены данные отзыва с id {}", review.getReviewId());
            } else {
                throw new NoSuchElementException("Отзыв с id " + review.getReviewId()
                        + " не найден. Обновление отклонено.");
            }
        }
        return getReview(review.getReviewId());
    }

    @Override
    public Map<String, String> removeReview(int reviewId) {
        String deleteReviewQuery = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        int linesDelete = jdbcTemplate.update(deleteReviewQuery, reviewId);
        if (linesDelete > 0) {
            log.info("Удалён отзыв с id {}", reviewId);
            return Map.of("result", "Удалён отзыв с id " + reviewId);
        } else {
            throw new NoSuchElementException("Сведения об отзыве с id " + reviewId
                    + " не найдены. Удаление отклонено.");
        }
    }

    @Override
    public Review getReview(int reviewId) {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet(commonPartOfQuery() + " HAVING r.REVIEW_ID = ?",
                reviewId);
        List<Review> oneReview = reviewsParsing(reviewRows);
        if (oneReview.size() == 1) {
            return oneReview.get(0);
        } else {
            log.info("Отзыв с идентификатором {} не найден.", reviewId);
            throw new NoSuchElementException("Отзыв с id " + reviewId + " не найден.");
        }
    }

    @Override
    public List<Review> findAll(int id, int count) {
        SqlRowSet reviewRows;
        if (id == 0) {
            reviewRows = jdbcTemplate.queryForRowSet(commonPartOfQuery() + " ORDER BY USEFUL DESC LIMIT ?", count);
        } else {
            reviewRows = jdbcTemplate.queryForRowSet(commonPartOfQuery() + " HAVING r.FILM_ID = ? " +
                            "ORDER BY USEFUL DESC LIMIT ?", id, count);
        }
        return reviewsParsing(reviewRows);
    }

    private String commonPartOfQuery() {
        return "SELECT r.REVIEW_ID, r.USER_ID, r.FILM_ID, r.CONTENT, d.DIRECTION, COALESCE(SUM(g.GRADE), 0) AS USEFUL "
                + "FROM REVIEWS r LEFT JOIN DIRECTIONS d ON r.DIRECTION_ID = d.DIRECTION_ID " +
                               "LEFT JOIN OPINIONS o ON r.REVIEW_ID = o.REVIEW_ID " +
                               "LEFT JOIN GRADES g ON o.GRADE_ID = g.GRADE_ID " +
                "GROUP BY r.REVIEW_ID";
    }

    private List<Review> reviewsParsing(SqlRowSet reviewRows) {
        List<Review> reviews = new ArrayList<>();
        while (reviewRows.next()) {
            Review review = new Review(
                    Integer.parseInt(Objects.requireNonNull(reviewRows.getString("REVIEW_ID"))),
                    reviewRows.getString("CONTENT"),
                    Boolean.parseBoolean(reviewRows.getString("DIRECTION")),
                    Integer.parseInt(Objects.requireNonNull(reviewRows.getString("USER_ID"))),
                    Integer.parseInt(Objects.requireNonNull(reviewRows.getString("FILM_ID"))),
                    Integer.parseInt(Objects.requireNonNull(reviewRows.getString("USEFUL"))));
            reviews.add(review);
        }
        return reviews;
    }

    @Override
    public Map<String, String> addOpinionPositive(int reviewId, int userId) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("OPINIONS")
                .usingGeneratedKeyColumns("OPINION_ID");
        int returningKey = simpleJdbcInsert.executeAndReturnKey(opinionsToMap(reviewId, userId, GRADE_ID_POSITIVE))
                .intValue();
        if (returningKey > 0) {
            log.info("Пользователь с id {} положительно оценил отзыв с id {}", userId, reviewId);
            return Map.of("result", "Пользователь с id " + userId + " положительно оценил отзыв с id "
                    + reviewId);
        } else {
            throw new NoSuchElementException("Пользователь с id " + userId + " или отзыв с id " + reviewId +
                    " не найдены. Добавление оценки отзыва отклонено.");
        }
    }

    @Override
    public Map<String, String> addOpinionNegative(int reviewId, int userId) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("OPINIONS")
                .usingGeneratedKeyColumns("OPINION_ID");
        int returningKey = simpleJdbcInsert.executeAndReturnKey(opinionsToMap(reviewId, userId, GRADE_ID_NEGATIVE))
                .intValue();
        if (returningKey > 0) {
            log.info("Пользователь с id {} отрицательно оценил отзыв с id {}", userId, reviewId);
            return Map.of("result", "Пользователь с id " + userId + " отрицательно оценил отзыв с id "
                    + reviewId);
        } else {
            throw new NoSuchElementException("Пользователь с id " + userId + " или отзыв с id " + reviewId +
                    " не найдены. Добавление оценки отзыва отклонено.");
        }
    }

    private Map<String, Integer> opinionsToMap(int reviewId, int userId, int gradeId) {
        Map<String, Integer> values = new HashMap<>();
        values.put("REVIEW_ID", reviewId);
        values.put("USER_ID", userId);
        values.put("GRADE_ID", gradeId);
        return values;
    }

    @Override
    public Map<String, String> removeOpinionPositive(int reviewId, int userId) {
        String sqlQuery = "DELETE FROM OPINIONS WHERE REVIEW_ID = ? AND USER_ID = ?";
        int linesDelete = jdbcTemplate.update(sqlQuery, reviewId, userId);
        if (linesDelete > 0) {
            log.info("Пользователь с id {} удалил положительную оценку отзыва с id {}", userId, reviewId);
            return Map.of("result", "Пользователь с id " + userId + " удалил положительную оценку " +
                    "отзыва с id " + reviewId);
        } else {
            throw new NoSuchElementException("Сведения о положительной оценке от пользователя с id " + userId +
                    " отзыву с id " + reviewId + " не найдены. Удаление оценки отклонено.");
        }
    }

    @Override
    public Map<String, String> removeOpinionNegative(int reviewId, int userId) {
        String sqlQuery = "DELETE FROM OPINIONS WHERE REVIEW_ID = ? AND USER_ID = ?";
        int linesDelete = jdbcTemplate.update(sqlQuery, reviewId, userId);
        if (linesDelete > 0) {
            log.info("Пользователь с id {} удалил отрицательную оценку отзыва с id {}", userId, reviewId);
            return Map.of("result", "Пользователь с id " + userId + " удалил отрицательную оценку " +
                    "отзыва с id " + reviewId);
        } else {
            throw new NoSuchElementException("Сведения об отрицательной оценке от пользователя с id " + userId +
                    " отзыву с id " + reviewId + " не найдены. Удаление оценки отклонено.");
        }
    }
}
