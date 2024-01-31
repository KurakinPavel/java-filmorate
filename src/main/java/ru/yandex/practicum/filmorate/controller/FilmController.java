package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    protected int numerator;
    protected final Map<Integer, Film> films;

    public FilmController() {
        numerator = 0;
        films = new HashMap<>();
    }

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        film.setId(++numerator);
        films.put(numerator, film);
        log.info("Добавлен новый фильм с id {} и названием {}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
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
