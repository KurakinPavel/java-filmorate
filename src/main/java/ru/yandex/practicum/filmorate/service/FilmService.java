package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage; //Shtefan добавление нового сторейджа режиссёров

    @Autowired
    public FilmService(FilmStorage filmStorage, MpaStorage mpaStorage, //Shtefan добавление нового сторейджа режиссёров
                       GenreStorage genreStorage, DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;

    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        setMpaAndGenres(film);
        setDirectors(film); //SHTEFAN добавление режиссёров
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

    private void setDirectors(Film film) { //SHTEFAN добавление режиссёров
        try {
            if (film.getDirectors() != null) {
                List<Integer> directorsInInt = film.directorsToInt();
                List<Director> directors = directorStorage.someDirectors(directorsInInt);
                if (directorsInInt.size() != directors.size())
                    throw new NoSuchElementException("Переданы некорректные id режиссёров.");
                film.setDirectors(directors);
            }
        } catch (NoSuchElementException exception) {
            throw new IllegalArgumentException("Фильм с названием '" + film.getName() + "' не создан. "
                    + exception.getMessage());
        }

    }

    public Film update(Film film) {
        setMpaAndGenres(film);
        setDirectors(film);
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

    public List<Film> getByDirector(Integer id, String sortBy) { //SHTEFAN поиск по режиссёру
        directorStorage.getDirector(id);
        if (sortBy.equals("year"))
            return filmStorage.getByDirector(id, " f.RELEASE_DATE ASC");
        else if (sortBy.equals("likes"))
            return filmStorage.getByDirector(id, " POPULAR_FILMS.POPULARITY DESC");
        else throw new IllegalArgumentException("Неправильный формат сортировки");


    }
}
