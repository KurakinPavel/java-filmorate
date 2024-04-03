package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, MpaStorage mpaStorage, GenreStorage genreStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        setMpaAndGenres(film);
        return filmStorage.create(film);
    }

    private void setMpaAndGenres(Film film) {
        try {
            film.setMpa(mpaStorage.getMPA(film.getMpa().getId()));
            if (film.getGenres() != null) {
                List<Integer> genresInInt = film.genresToInt();
                List<Genre> genres = genreStorage.someGenres(genresInInt);
                if (genresInInt.size() != genres.size())
                    throw new NoSuchElementException("Переданы некорректные id жанров.");
                film.setGenres(genres);
            }
        } catch (NoSuchElementException exception) {
            throw new IllegalArgumentException("Фильм с названием '" + film.getName() + "' не создан. "
                    + exception.getMessage());
        }
    }

    public Film update(Film film) {
        setMpaAndGenres(film);
        return filmStorage.update(film);
    }

    public Film getFilm(int id) {
        return filmStorage.getFilm(id);
    }

    public Map<String, String> addLike(int id, int userId) {
        return filmStorage.addLike(id, userId);
    }

    public Map<String, String> removeLike(int id, int userId) {
        return filmStorage.removeLike(id, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }
}
