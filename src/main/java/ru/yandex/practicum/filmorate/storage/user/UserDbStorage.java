package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS");
        return usersParsing(userRows);
    }

    @Override
    public User getUser(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE USER_ID = ?", id);
        List<User> oneUser = usersParsing(userRows);
        if (oneUser.size() == 1) {
            return oneUser.get(0);
        } else {
            log.info("Пользователь с идентификатором {} не найден.", id);
            throw new NoSuchElementException("Пользователь с id " + id + " не найден.");
        }
    }

    private List<User> usersParsing(SqlRowSet usersRows) {
        List<User> users = new ArrayList<>();
        while (usersRows.next()) {
            User user = new User(
                    Integer.parseInt(Objects.requireNonNull(usersRows.getString("USER_ID"))),
                    usersRows.getString("EMAIL"),
                    usersRows.getString("LOGIN"),
                    usersRows.getString("NAME"),
                    LocalDate.parse(Objects.requireNonNull(usersRows.getString("BIRTHDAY"))));
            users.add(user);
        }
        return users;
    }

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("USER_ID");
        user.setId(simpleJdbcInsert.executeAndReturnKey(user.userToMap()).intValue());
        log.info("Добавлен новый пользователь с id {} и логином {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == 0) {
            create(user);
        } else {
            String sqlQuery = "UPDATE USERS SET " +
                    "EMAIL = ?, LOGIN = ?, NAME = ?, " +
                    "BIRTHDAY = ? WHERE USER_ID = ?";
            int linesChanged = jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId());
            if (linesChanged > 0) {
                log.info("Обновлены данные пользователя с id {} и логином {}", user.getId(), user.getLogin());
            } else {
                throw new NoSuchElementException("Пользователь с id " + user.getId() + " и логином " + user.getLogin()
                        + " не найден. Обновление отклонено.");
            }
        }
        return user;
    }

    @Override
    public Map<String, String> addInFriends(int id, int friendId) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FRIENDS")
                .usingGeneratedKeyColumns("FRIENDS_ID");
        int returningKey = simpleJdbcInsert.executeAndReturnKey(friendsToMap(id, friendId)).intValue();
        if (returningKey > 0) {
            log.info("Пользователь с id {} добавился в друзья к пользователю с id {}", id, friendId);
            return Map.of("result", "Пользователь с id " + id + " добавился в друзья к пользователю с id "
                    + friendId);
        } else {
            throw new NoSuchElementException("Пользователь с id " + id + " или пользователь с id " + friendId
                    + " не найден. Добавление в друзья отклонено.");
        }
    }

    private Map<String, Integer> friendsToMap(int id, int friendId) {
        Map<String, Integer> values = new HashMap<>();
        values.put("USER_ID", id);
        values.put("FRIEND_ID", friendId);
        return values;
    }

    @Override
    public Map<String, String> removeFromFriends(int id, int friendId) {
        friendsVerification(id, friendId);
        String sqlQuery = "DELETE FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        int linesDelete = jdbcTemplate.update(sqlQuery, id, friendId);
        if (linesDelete > 0) {
            log.info("Пользователь с id {} удалился из друзей у пользователя с id {}", id, friendId);
            return Map.of("result", "Пользователь с id " + id + " удалился из друзей у пользователя с id "
                    + friendId);
        } else {
            log.info("Сведения о дружбе между пользователями с id {} и id {} отсутствуют. Удалять нечего.",
                    id, friendId);
            return Map.of("result", "Пользователь с id " + id + " не был в друзьях у пользователя с id "
                    + friendId + ". Удалять нечего.");
        }
    }

    private void friendsVerification(int id, int friendId) {
        List<Integer> usersIdForVerification = new ArrayList<>();
        usersIdForVerification.add(id);
        usersIdForVerification.add(friendId);
        List<User> usersForVerification = someUsers(usersIdForVerification);
        Set<Integer> pairForVerification = new HashSet<>();
        for (User user : usersForVerification) {
            pairForVerification.add(user.getId());
        }
        if (pairForVerification.isEmpty()) {
            throw new NoSuchElementException("Переданы некорректные идентификаторы пользователей: " +
                    id + " и " + friendId);
        } else if (!pairForVerification.contains(id)) {
            throw new NoSuchElementException("Передан некорректный id пользователя: " + id);
        } else if (!pairForVerification.contains(friendId)) {
            throw new NoSuchElementException("Передан некорректный friendId пользователя: " + friendId);
        }
    }

    @Override
    public List<User> getFriendsOfUser(int id) {
        List<Integer> userFriendsId = new ArrayList<>(getFriendsId(id));
        return someUsers(userFriendsId);
    }

    public List<User> someUsers(List<Integer> usersId) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        String sql = "SELECT * FROM USERS WHERE USER_ID IN (:values)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("values", usersId);

        return namedParameterJdbcTemplate.query(
                sql, parameters,
                (rs, rowNow) -> new User(rs.getInt("USER_ID"),
                        rs.getString("EMAIL"),
                        rs.getString("LOGIN"),
                        rs.getString("NAME"),
                        LocalDate.parse(rs.getString("BIRTHDAY"))));
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        Set<Integer> userFriends = getFriendsId(id);
        Set<Integer> otherUserFriends = getFriendsId(otherId);
        Set<Integer> intersection = new HashSet<>(userFriends);
        intersection.retainAll(otherUserFriends);
        List<Integer> commonFriendsId = new ArrayList<>(intersection);
        return someUsers(commonFriendsId);
    }

    private Set<Integer> getFriendsId(int id) {
        Set<Integer> friendsId = new HashSet<>();
        int lineCounter = 0;

        SqlRowSet friendsIdRows = jdbcTemplate.queryForRowSet("SELECT u.USER_ID, f.FRIEND_ID FROM USERS u " +
                "LEFT JOIN FRIENDS f ON u.USER_ID = f.USER_ID WHERE u.USER_ID = ?", id);
        while (friendsIdRows.next()) {
            lineCounter++;
            String friendIdValue = friendsIdRows.getString("FRIEND_ID");
            if (friendIdValue != null) {
                int friendId = Integer.parseInt(friendIdValue);
                friendsId.add(friendId);
            }
        }

        if (lineCounter == 0) {
            log.info("Пользователь с идентификатором {} не найден. Получить список его друзей не удалось.", id);
            throw new NoSuchElementException("Пользователь с id " + id + " в базе отсутствует. " +
                    "Получить список его друзей не удалось.");
        } else {
            return friendsId;
        }
    }
}
