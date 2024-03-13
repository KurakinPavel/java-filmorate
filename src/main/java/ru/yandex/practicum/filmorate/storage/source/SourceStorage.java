package ru.yandex.practicum.filmorate.storage.source;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.IdAndNameContainer;

import java.util.List;

@Component
public interface SourceStorage {

    List<IdAndNameContainer> allGenres();

    IdAndNameContainer getGenre(int id);

    List<IdAndNameContainer> allMPA();

    IdAndNameContainer getMPA(int id);

}
