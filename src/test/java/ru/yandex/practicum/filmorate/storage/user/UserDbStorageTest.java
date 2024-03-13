package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    void findAllTest() {
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User newUser1 = new User(0,"user1@email.ru", "vanya1123", "IIvan Petrov",
                LocalDate.of(1990, 1, 1));
        User newUser2 = new User(0,"user2@email.ru", "vanya1223", "IIIvan Petrov",
                LocalDate.of(1990, 2, 1));
        userStorage.create(newUser1);
        userStorage.create(newUser2);
        List<User> users = userStorage.findAll();
        Assertions.assertEquals(2, users.size(), "Количество полученных из базы объектов "
                + "не совпадает с ожидаемым");
    }

    @Test
    void getUserTest() {
        User newUser = new User(0,"user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.create(newUser);
        User savedUser = userStorage.getUser(newUser.getId());
        Assertions.assertEquals(savedUser, newUser, "Созданный и извлечённый объекты не совпадают");
    }

    @Test
    void updateTest() {
        User newUser = new User(0,"user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.create(newUser);
        int id = newUser.getId();
        User userForUpdate = new User(id,"user1@email.ru", "vanya1123", "IIvan Petrov",
                LocalDate.of(1990, 2, 1));
        userStorage.update(userForUpdate);
        User updatedUser = userStorage.getUser(id);
        Assertions.assertEquals(updatedUser, userForUpdate, "Созданный и обновлённый объекты не совпадают");
    }

    @Test
    void addInFriendsAndGetFriendsOfUserTest() {
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User user1 = new User(0,"user1@email.ru", "vanya1123", "IIvan Petrov",
                LocalDate.of(1990, 1, 1));
        User user2 = new User(0,"user2@email.ru", "vanya1223", "IIIvan Petrov",
                LocalDate.of(1990, 2, 1));
        userStorage.create(user1);
        userStorage.create(user2);
        userStorage.addInFriends(user1.getId(), user2.getId());
        List<User> friendsOfUser1 = userStorage.getFriendsOfUser(user1.getId());
        Assertions.assertEquals(1, friendsOfUser1.size(), "У целевого пользователя не "
                + "появилось друзей");
        Assertions.assertEquals(user2.getId(), friendsOfUser1.get(0).getId(), "Дружба не создалась");
    }

    @Test
    void removeFromFriendsTest() {
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User user1 = new User(0,"user1@email.ru", "vanya1123", "IIvan Petrov",
                LocalDate.of(1990, 1, 1));
        User user2 = new User(0,"user2@email.ru", "vanya1223", "IIIvan Petrov",
                LocalDate.of(1990, 2, 1));
        userStorage.create(user1);
        userStorage.create(user2);
        userStorage.addInFriends(user1.getId(), user2.getId());
        List<User> friendsOfUser1 = userStorage.getFriendsOfUser(user1.getId());
        Assertions.assertEquals(1, friendsOfUser1.size(), "У целевого пользователя не "
                + "появилось друзей");
        userStorage.removeFromFriends(user1.getId(), user2.getId());
        friendsOfUser1 = userStorage.getFriendsOfUser(user1.getId());
        Assertions.assertEquals(0, friendsOfUser1.size(), "У целевого пользователя не "
                + "должно остаться друзей");
    }

    @Test
    void getCommonFriendsTest() {
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User user1 = new User(0,"user1@email.ru", "vanya1123", "IIvan Petrov",
                LocalDate.of(1990, 1, 1));
        User user2 = new User(0,"user2@email.ru", "vanya1223", "IIIvan Petrov",
                LocalDate.of(1990, 2, 1));
        User user3 = new User(0,"user3@email.ru", "vanya1323", "IIIIvan Petrov",
                LocalDate.of(1990, 3, 1));
        userStorage.create(user1);
        userStorage.create(user2);
        userStorage.create(user3);
        userStorage.addInFriends(user1.getId(), user3.getId());
        userStorage.addInFriends(user2.getId(), user3.getId());
        List<User> friendsOfUser1 = userStorage.getFriendsOfUser(user1.getId());
        List<User> friendsOfUser2 = userStorage.getFriendsOfUser(user2.getId());
        Assertions.assertEquals(friendsOfUser1.get(0), friendsOfUser2.get(0), "Друзья не совпадают");
    }
}
