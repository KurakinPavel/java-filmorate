package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

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

    public List<Film> getRecommendedFilms(Integer id) {
        return filmStorage.getRecommendedFilms(id);
    }
}
