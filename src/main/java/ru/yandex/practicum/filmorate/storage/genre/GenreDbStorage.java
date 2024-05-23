package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Component
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> allGenres() {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES ORDER BY GENRE_ID");
        return genresParsing(genresRows);
    }

    @Override
    public List<Genre> someGenres(List<Integer> genresInInt) {
        NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        String sql = "SELECT GENRE_ID, GENRE FROM GENRES WHERE GENRE_ID IN (:values)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("values", genresInInt);

        return namedTemplate.query(
                sql, parameters,
                (rs, rowNow) -> new Genre(rs.getInt("GENRE_ID"), rs.getString("GENRE")));
    }

    @Override
    public Genre getGenre(int id) {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE GENRE_ID = ?", id);
        List<Genre> oneGenre = genresParsing(genresRows);
        if (oneGenre.size() == 1) {
            return oneGenre.get(0);
        } else {
            log.info("Жанр с идентификатором {} не найден.", id);
            throw new NoSuchElementException("Жанр с id " + id + " не найден.");
        }
    }

    private List<Genre> genresParsing(SqlRowSet genresRows) {
        List<Genre> genres = new ArrayList<>();
        while (genresRows.next()) {
            Genre genre = new Genre(
                    Integer.parseInt(Objects.requireNonNull(genresRows.getString("GENRE_ID"))),
                    genresRows.getString("GENRE"));
            genres.add(genre);
        }
        return genres;
    }
}
