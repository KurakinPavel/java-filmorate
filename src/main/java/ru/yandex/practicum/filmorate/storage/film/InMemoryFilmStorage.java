package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    protected int numerator;
    protected final Map<Integer, Film> films;

    public InMemoryFilmStorage() {
        numerator = 0;
        films = new HashMap<>();
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(int id) {
        if (!films.containsKey(id)) {
            throw new NoSuchElementException("Фильм с id " + id + " не найден.");
        } else {
            return films.get(id);
        }
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
}
