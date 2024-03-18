package ru.yandex.practicum.filmorate.storage.source;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MpaAndGenreDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    void allGenresTest() {
        GenreDbStorage genreStorage = new GenreDbStorage(jdbcTemplate);
        List<Genre> allGenres = genreStorage.allGenres();
        Assertions.assertEquals(6, allGenres.size(), "Количество жанров в базе " +
                "отличается от ожидаемого");
    }

    @Test
    void getGenreTest() {
        GenreDbStorage genreStorage = new GenreDbStorage(jdbcTemplate);
        Genre genre = genreStorage.getGenre(5);
        Assertions.assertEquals("Документальный", genre.getName(), "Полученный по запросу жанр "
                + "отличается от ожидаемого");
    }

    @Test
    void allMPATest() {
        MpaDbStorage mpaStorage = new MpaDbStorage(jdbcTemplate);
        List<Mpa> allMPA = mpaStorage.allMPA();
        Assertions.assertEquals(5, allMPA.size(), "Количество возрастных рейтингов в базе " +
                "отличается от ожидаемого");
    }

    @Test
    void getMPATest() {
        MpaDbStorage mpaStorage = new MpaDbStorage(jdbcTemplate);
        Mpa mpa = mpaStorage.getMPA(5);
        Assertions.assertEquals("NC-17", mpa.getName(), "Полученный по запросу возрастной рейтинг "
                + "отличается от ожидаемого");
    }
}