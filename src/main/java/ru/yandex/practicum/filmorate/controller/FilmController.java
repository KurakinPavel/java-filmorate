package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        return filmStorage.update(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Integer id) {
        return filmStorage.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Map<String, String> addLike(@PathVariable Integer id,
                                       @PathVariable Integer userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Map<String, String> removeLike(@PathVariable Integer id,
                                          @PathVariable Integer userId) {
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return filmService.getPopularFilms(count);
    }
}
