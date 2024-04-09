package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.*;

import static ru.yandex.practicum.filmorate.model.Constants.*;

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
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(review.reviewToMap(getDirectionIdFromDB(review.getIsPositive()))).intValue());
        log.info("Добавлен новый отзыв с id {}", review.getReviewId());
        Review createdReview = getReview(review.getReviewId());
        addEvent(createdReview.getUserId(), createdReview.getReviewId(), getOperationIdFromDB("ADD"));
        return createdReview;
    }

    private int getDirectionIdFromDB(Boolean isPositive) {
        String directionOfReview = String.valueOf(isPositive);
        int directionIdFromDB = 0;
        String sql = "SELECT DIRECTION_ID FROM DIRECTIONS WHERE DIRECTION = ?";
        try {
            Integer g = jdbcTemplate.queryForObject(sql, Integer.class, directionOfReview);
            if (g != null) {
                directionIdFromDB = g;
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchElementException("Ключ для характера ревью " + directionOfReview + " не найден");
        }
        return directionIdFromDB;
    }

    @Override
    public Review update(Review review) {
        Review createdOrUpdatedReview;
        if (review.getReviewId() == 0) {
            createdOrUpdatedReview = create(review);
        } else {
            String sqlQuery = "UPDATE REVIEWS SET " +
                    "CONTENT = ?, DIRECTION_ID = ? WHERE REVIEW_ID = ?";
            int linesChanged = jdbcTemplate.update(sqlQuery,
                    review.getContent(),
                    getDirectionIdFromDB(review.getIsPositive()),
                    review.getReviewId());
            if (linesChanged > 0) {
                createdOrUpdatedReview = getReview(review.getReviewId());
                addEvent(createdOrUpdatedReview.getUserId(), createdOrUpdatedReview.getReviewId(), getOperationIdFromDB("UPDATE"));
                log.info("Обновлены данные отзыва с id {}", review.getReviewId());
            } else {
                throw new NoSuchElementException("Отзыв с id " + review.getReviewId()
                        + " не найден. Обновление отклонено.");
            }
        }
        return createdOrUpdatedReview;
    }

    @Override
    public Map<String, String> removeReview(int reviewId) {
        Review removingReview = getReview(reviewId);
        String deleteReviewQuery = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        jdbcTemplate.update(deleteReviewQuery, reviewId);
        addEvent(removingReview.getUserId(), removingReview.getReviewId(), getOperationIdFromDB("REMOVE"));
        log.info("Удалён отзыв с id {}", reviewId);
        return Map.of("result", "Удалён отзыв с id " + reviewId);
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
        return "SELECT r.REVIEW_ID, r.USER_ID, r.FILM_ID, r.CONTENT, d.DIRECTION, SUM(COALESCE(g.GRADE, 0)) AS USEFUL "
                + "FROM REVIEWS r LEFT JOIN DIRECTIONS d ON r.DIRECTION_ID = d.DIRECTION_ID " +
                               "LEFT JOIN OPINIONS o ON r.REVIEW_ID = o.REVIEW_ID " +
                               "LEFT JOIN GRADES g ON o.GRADE_ID = g.GRADE_ID " +
                "GROUP BY r.REVIEW_ID";
    }

    private List<Review> reviewsParsing(SqlRowSet reviewRows) {
        List<Review> reviews = new ArrayList<>();
        while (reviewRows.next()) {
            Review review = new Review(
                    reviewRows.getInt("REVIEW_ID"),
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
        int returningKey = simpleJdbcInsert.executeAndReturnKey(opinionsToMap(reviewId, userId, GRADE_POSITIVE))
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
        int returningKey = simpleJdbcInsert.executeAndReturnKey(opinionsToMap(reviewId, userId, GRADE_NEGATIVE))
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

    private Map<String, Integer> opinionsToMap(int reviewId, int userId, int grade) {
        Map<String, Integer> values = new HashMap<>();
        values.put("REVIEW_ID", reviewId);
        values.put("USER_ID", userId);
        values.put("GRADE_ID", getGradeIdFromDB(grade));
        return values;
    }

    private int getGradeIdFromDB(int grade) {
        int gradeId = 0;
        String sql = "SELECT GRADE_ID FROM GRADES WHERE GRADE = ?";
        try {
            Integer g = jdbcTemplate.queryForObject(sql, Integer.class, grade);
            if (g != null) {
                gradeId = g;
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchElementException("Ключ для значения grade " + grade + " не найден");
        }
        return gradeId;
    }

    @Override
    public Map<String, String> removeOpinion(int reviewId, int userId) {
        String sqlQuery = "DELETE FROM OPINIONS WHERE REVIEW_ID = ? AND USER_ID = ?";
        int linesDelete = jdbcTemplate.update(sqlQuery, reviewId, userId);
        if (linesDelete > 0) {
            log.info("Пользователь с id {} удалил оценку отзыва с id {}", userId, reviewId);
            return Map.of("result", "Пользователь с id " + userId + " удалил оценку " +
                    "отзыва с id " + reviewId);
        } else {
            throw new NoSuchElementException("Сведения об оценке от пользователя с id " + userId +
                    " отзыву с id " + reviewId + " не найдены. Удаление оценки отклонено.");
        }
    }

    private void addEvent(int userId, int entityId, int operationId) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("EVENTS")
                .usingGeneratedKeyColumns("EVENT_ID", "TIME_STAMP");
        simpleJdbcInsert.execute(Event.eventToMap(userId, entityId,
                getEventTypeIdFromDB("REVIEW"), operationId));
    }

    private int getEventTypeIdFromDB(String event) {
        int eventTypeId = 0;
        String sql = "SELECT TYPE_ID FROM EVENT_TYPES WHERE EVENT_TYPE = ?";
        try {
            Integer g = jdbcTemplate.queryForObject(sql, Integer.class, event);
            if (g != null) {
                eventTypeId = g;
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchElementException("Ключ для события " + event + " не найден");
        }
        return eventTypeId;
    }

    private int getOperationIdFromDB(String operation) {
        int operationId = 0;
        String sql = "SELECT OPERATION_ID FROM OPERATIONS WHERE OPERATION = ?";
        try {
            Integer g = jdbcTemplate.queryForObject(sql, Integer.class, operation);
            if (g != null) {
                operationId = g;
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchElementException("Ключ для операции " + operationId + " не найден");
        }
        return operationId;
    }
}
