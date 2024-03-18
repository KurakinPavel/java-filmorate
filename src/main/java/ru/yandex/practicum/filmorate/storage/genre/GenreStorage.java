package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Component
public interface GenreStorage {

    List<Genre> allGenres();

    List<Genre> someGenres(List<Integer> genresInInt);

    Genre getGenre(int id);

}
