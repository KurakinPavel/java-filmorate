package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.IdContainer;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Primary
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
        String sqlQuery = "DELETE FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        int linesDelete = jdbcTemplate.update(sqlQuery, id, friendId);
        if (linesDelete > 0) {
            log.info("Пользователь с id {} удалился из друзей у пользователя с id {}", id, friendId);
            return Map.of("result", "Пользователь с id " + id + " удалился из друзей у пользователя с id "
                    + friendId);
        } else {
            throw new NoSuchElementException("Данные о дружбе между пользователем с id " + id + " и пользователем "
                    + "с id " + friendId + " не найдены. Удаление из друзей отклонено.");
        }
    }

    @Override
    public List<User> getFriendsOfUser(int id) {
        List<User> friends = new ArrayList<>();
        for (int friendId : getFriendsId(id)) {
            friends.add(getUser(friendId));
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        Set<Integer> userFriends = getFriendsId(id);
        Set<Integer> otherUserFriends = getFriendsId(otherId);
        Set<Integer> intersection = new HashSet<>(userFriends);
        intersection.retainAll(otherUserFriends);
        List<User> commonFriends = new ArrayList<>();
        for (int friendId : intersection) {
            commonFriends.add(getUser(friendId));
        }
        return commonFriends;
    }

    private Set<Integer> getFriendsId(int id) {
        SqlRowSet friendsIdRows = jdbcTemplate.queryForRowSet("SELECT FRIEND_ID FROM FRIENDS WHERE " +
                "USER_ID = ?", id);
        Set<Integer> friendsId = new HashSet<>();
        while (friendsIdRows.next()) {
            int friendId = Integer.parseInt(Objects.requireNonNull(friendsIdRows.getString("FRIEND_ID")));
            friendsId.add(friendId);
        }
        return friendsId;
    }
}
