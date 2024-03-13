package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.IdAndNameContainer;
import ru.yandex.practicum.filmorate.service.SourceService;

import java.util.List;

@RestController
public class SourceController {
    private final SourceService sourceService;

    @Autowired
    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @GetMapping("/genres")
    public List<IdAndNameContainer> allGenres() {
        return sourceService.allGenres();
    }

    @GetMapping("/genres/{id}")
    public IdAndNameContainer getGenre(@PathVariable Integer id) {
        return sourceService.getGenre(id);
    }

    @GetMapping("/mpa")
    public List<IdAndNameContainer> allMPA() {
        return sourceService.allMPA();
    }

    @GetMapping("/mpa/{id}")
    public IdAndNameContainer getMPA(@PathVariable Integer id) {
        return sourceService.getMPA(id);
    }


}
