package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    protected int numerator;
    protected final Map<Integer, Film> films;
    private final UserStorage userStorage;

    @Autowired
    public InMemoryFilmStorage(UserStorage userStorage) {
        this.userStorage = userStorage;
        numerator = 0;
        films = new HashMap<>();
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(int id) {
        Film film = films.get(id);
        if (film == null) throw new NoSuchElementException("Фильм с id " + id + " не найден.");
        return film;
    }

    @Override
    public Film create(Film film) {
        film.setId(++numerator);
        films.put(numerator, film);
        log.info("Добавлен новый фильм с id {} и названием {}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == 0) {
            film.setId(++numerator);
            films.put(numerator, film);
            log.info("Добавлен новый фильм с id {} и названием {}", film.getId(), film.getName());
        } else if (!films.containsKey(film.getId())) {
            throw new NoSuchElementException("Фильм с id " + film.getId() + " и названием " + film.getName() +
                    " не найден. Обновление отклонено.");
        } else {
            films.put(film.getId(), film);
            log.info("Обновлены данные фильма с id {} и названием {}", film.getId(), film.getName());
        }
        return film;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return findAll().stream()
                .sorted(Comparator.comparing(Film::getLikesSize).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> addLike(int id, int userId) {
        User user = userStorage.getUser(userId);
        Set<Integer> filmLikes = films.get(id).getLikes();
        filmLikes.add(userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, id);
        return Map.of("result", "Пользователь с id " + userId + " поставил лайк фильму с id " + id);
    }

    @Override
    public Map<String, String> removeLike(int id, int userId) {
        User user = userStorage.getUser(userId);
        Set<Integer> filmLikes = films.get(id).getLikes();
        filmLikes.remove(userId);
        log.info("Пользователь с id {} удалил лайк фильму с id {}", userId, id);
        return Map.of("result", "Пользователь с id " + userId + " удалил лайк фильму с id " + id);
    }
}
