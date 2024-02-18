package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film getFilm(int id) {
        return filmStorage.getFilm(id);
    }

    public Map<String, String> addLike(int id, int userId) {
        User user = userStorage.getUser(userId);
        Set<Integer> filmLikes = filmStorage.getFilm(id).getLikes();
        filmLikes.add(userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, id);
        return Map.of("result", "Пользователь с id " + userId + " поставил лайк фильму с id " + id);
    }

    public Map<String, String> removeLike(int id, int userId) {
        User user = userStorage.getUser(userId);
        Set<Integer> filmLikes = filmStorage.getFilm(id).getLikes();
        filmLikes.remove(userId);
        log.info("Пользователь с id {} удалил лайк фильму с id {}", userId, id);
        return Map.of("result", "Пользователь с id " + userId + " удалил лайк фильму с id " + id);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}
