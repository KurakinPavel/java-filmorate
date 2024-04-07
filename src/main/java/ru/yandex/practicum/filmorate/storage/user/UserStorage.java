package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

@Component
public interface UserStorage {

    List<User> findAll();

    User getUser(int id);

    User create(User user);

    User update(User user);

    Map<String, String> addInFriends(int id, int friendId);

    Map<String, String> removeFromFriends(int id, int friendId);

    List<User> getFriendsOfUser(int id);

    List<User> getCommonFriends(int id, int otherId);
}
