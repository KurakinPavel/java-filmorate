package ru.yandex.practicum.filmorate.storage.director;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Component
public interface DirectorStorage {
    List<Director> getAll();

    Director getDirector(int id);

    Director create(Director director);

    Director update(Director director);

    List<Director> someDirectors(List<Integer> directorsInInt);

    void delete(int id);
}
