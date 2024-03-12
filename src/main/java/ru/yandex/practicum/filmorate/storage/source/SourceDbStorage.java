package ru.yandex.practicum.filmorate.storage.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.IdAndNameContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Component
public class SourceDbStorage implements SourceStorage {
    private final JdbcTemplate jdbcTemplate;

    public SourceDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<IdAndNameContainer> allGenres() {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES ORDER BY GENRE_ID");
        return genresParsing(genresRows);
    }

    @Override
    public IdAndNameContainer getGenre(int id) {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE GENRE_ID = ?", id);
        List<IdAndNameContainer> oneGenre = genresParsing(genresRows);
        if (oneGenre.size() == 1) {
            return oneGenre.get(0);
        } else {
            log.info("Жанр с идентификатором {} не найден.", id);
            throw new NoSuchElementException("Жанр с id " + id + " не найден.");
        }
    }

    private List<IdAndNameContainer> genresParsing(SqlRowSet genresRows) {
        List<IdAndNameContainer> genres = new ArrayList<>();
        while (genresRows.next()) {
            IdAndNameContainer idAndNameContainer = new IdAndNameContainer(
                    Integer.parseInt(Objects.requireNonNull(genresRows.getString("GENRE_ID"))),
                    genresRows.getString("GENRE"));
            genres.add(idAndNameContainer);
        }
        return genres;
    }

    @Override
    public List<IdAndNameContainer> allMPA() {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM MPA ORDER BY MPA_ID");
        return mpaParsing(mpaRows);
    }

    @Override
    public IdAndNameContainer getMPA(int id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM MPA WHERE MPA_ID = ?", id);
        List<IdAndNameContainer> oneMPA = mpaParsing(mpaRows);
        if (oneMPA.size() == 1) {
            return oneMPA.get(0);
        } else {
            log.info("Возрастной рейтинг с идентификатором {} не найден.", id);
            throw new NoSuchElementException("Возрастной рейтинг с id " + id + " не найден.");
        }
    }

    private List<IdAndNameContainer> mpaParsing(SqlRowSet mpaRows) {
        List<IdAndNameContainer> mpaCatalog = new ArrayList<>();
        while (mpaRows.next()) {
            IdAndNameContainer idAndNameContainer = new IdAndNameContainer(
                    Integer.parseInt(Objects.requireNonNull(mpaRows.getString("MPA_ID"))),
                    mpaRows.getString("MPA"));
            mpaCatalog.add(idAndNameContainer);
        }
        return mpaCatalog;
    }
}
