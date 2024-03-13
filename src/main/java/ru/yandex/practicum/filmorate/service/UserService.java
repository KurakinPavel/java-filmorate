package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
        return userStorage.addInFriends(id, friendId);
    }

    public Map<String, String> removeFromFriends(int id, int friendId) {
        return userStorage.removeFromFriends(id, friendId);
    }

    public List<User> getFriendsOfUser(int id) {
        return userStorage.getFriendsOfUser(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

}
