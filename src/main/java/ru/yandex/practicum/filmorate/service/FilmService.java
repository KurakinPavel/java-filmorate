package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.IdAndNameContainer;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.source.SourceStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final SourceStorage sourceStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, SourceStorage sourceStorage) {
        this.filmStorage = filmStorage;
        this.sourceStorage = sourceStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateGenreAndMPA(film);
        return filmStorage.create(film);
    }

    private void validateGenreAndMPA(Film film) {
        try {
            IdAndNameContainer containerMPA = sourceStorage.getMPA(film.getMpa().getId());
            if (film.getGenres() != null) {
                List<Integer> genresInInt = film.genresToInt();
                for (int genreId : genresInInt) {
                    IdAndNameContainer containerGenre = sourceStorage.getGenre(genreId);
                }
            }
        } catch (NoSuchElementException exception) {
            throw new IllegalArgumentException("Фильм с названием '" + film.getName() + "' не создан. "
                    + exception.getMessage());
        }
    }

    public Film update(Film film) {
        validateGenreAndMPA(film);
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

}
