package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

@Component
public interface FilmStorage {

    List<Film> findAll();

    Film getFilm(int id);

    Film create(Film film);

    Film update(Film film);

    List<Film> getPopularFilms(int count);

    Map<String, String> addLike(int id, int userId);

    Map<String, String> removeLike(int id, int userId);

}
