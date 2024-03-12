package ru.yandex.practicum.filmorate.storage.source;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.IdAndNameContainer;

import java.util.List;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SourceDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    void allGenresTest() {
        SourceDbStorage sourceStorage = new SourceDbStorage(jdbcTemplate);
        List<IdAndNameContainer> allGenres = sourceStorage.allGenres();
        Assertions.assertEquals(6, allGenres.size(), "Количество жанров в базе " +
                "отличается от ожидаемого");
    }

    @Test
    void getGenreTest() {
        SourceDbStorage sourceStorage = new SourceDbStorage(jdbcTemplate);
        IdAndNameContainer genre = sourceStorage.getGenre(5);
        Assertions.assertEquals("Документальный", genre.getName(), "Полученный по запросу жанр "
                + "отличается от ожидаемого");
    }

    @Test
    void allMPATest() {
        SourceDbStorage sourceStorage = new SourceDbStorage(jdbcTemplate);
        List<IdAndNameContainer> allMPA = sourceStorage.allMPA();
        Assertions.assertEquals(5, allMPA.size(), "Количество возрастных рейтингов в базе " +
                "отличается от ожидаемого");
    }

    @Test
    void getMPATest() {
        SourceDbStorage sourceStorage = new SourceDbStorage(jdbcTemplate);
        IdAndNameContainer mpa = sourceStorage.getMPA(5);
        Assertions.assertEquals("NC-17", mpa.getName(), "Полученный по запросу возрастной рейтинг "
                + "отличается от ожидаемого");
    }
}