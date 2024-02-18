package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User getUser(int id) {
        return userStorage.getUser(id);
    }

    public Map<String, String> addInFriends(int id, int friendId) {
        Set<Integer> userFriends = userStorage.getUser(id).getFriends();
        Set<Integer> friendFriends = userStorage.getUser(friendId).getFriends();
        userFriends.add(friendId);
        friendFriends.add(id);
        log.info("Пользователи с id {} и {} теперь друзья", id, friendId);
        return Map.of("result", "Пользователи с id " + id + " и " + friendId + " теперь друзья");
    }

    public Map<String, String> removeFromFriends(int id, int friendId) {
        Set<Integer> userFriends = userStorage.getUser(id).getFriends();
        Set<Integer> friendFriends = userStorage.getUser(friendId).getFriends();
        userFriends.remove(friendId);
        friendFriends.remove(id);
        log.info("Пользователи с id {} и {} больше не друзья", id, friendId);
        return Map.of("result", "Пользователи с id " + id + " и " + friendId + " больше не друзья");
    }

    public List<User> getFriendsOfUser(int id) {
        Set<Integer> userFriends = userStorage.getUser(id).getFriends();
        List<User> friends = new ArrayList<>();
        for (int friendId : userFriends) {
            friends.add(userStorage.getUser(friendId));
        }
        return friends;
    }

    public List<User> getCommonFriends(int id, int otherId) {
        Set<Integer> userFriends = userStorage.getUser(id).getFriends();
        Set<Integer> otherUserFriends = userStorage.getUser(otherId).getFriends();
        Set<Integer> intersection = new HashSet<>(userFriends);
        intersection.retainAll(otherUserFriends);
        List<User> commonFriends = new ArrayList<>();
        for (int friendId : intersection) {
            commonFriends.add(userStorage.getUser(friendId));
        }
        return commonFriends;
    }
}
