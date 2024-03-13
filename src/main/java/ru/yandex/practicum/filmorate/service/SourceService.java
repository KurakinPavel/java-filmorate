package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.IdAndNameContainer;
import ru.yandex.practicum.filmorate.storage.source.SourceStorage;

import java.util.List;

@Slf4j
@Service
public class SourceService {
    private final SourceStorage sourceStorage;

    @Autowired
    public SourceService(SourceStorage sourceStorage) {
        this.sourceStorage = sourceStorage;
    }

    public List<IdAndNameContainer> allGenres() {
        return sourceStorage.allGenres();
    }

    public IdAndNameContainer getGenre(int id) {
        return sourceStorage.getGenre(id);
    }

    public List<IdAndNameContainer> allMPA() {
        return sourceStorage.allMPA();
    }

    public IdAndNameContainer getMPA(int id) {
        return sourceStorage.getMPA(id);
    }
}
