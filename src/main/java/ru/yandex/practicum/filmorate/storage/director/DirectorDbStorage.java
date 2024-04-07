package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getAll() {
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("SELECT * FROM DIRECTOR");
        return directorParsing(directorRows);
    }

    @Override
    public Director getDirector(int id) {
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("SELECT * FROM DIRECTOR WHERE DIRECTOR_ID = ?", id);
        List<Director> director = directorParsing(directorRows);
        if (director.size() == 1) {
            return director.get(0);
        } else {
            log.info("Режиссёр с идентификатором {} не найден.", id);
            throw new NoSuchElementException("Режиссёр с id " + id + " не найден.");
        }
    }

    @Override
    public Director create(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("DIRECTOR")
                .usingGeneratedKeyColumns("DIRECTOR_ID");
        director.setId(simpleJdbcInsert.executeAndReturnKey(director.directorToMap()).intValue());
        log.info("Добавлен новый режиссёр с айди {}", director.getId());
        return director;
    }

    @Override
    public Director update(Director director) {
        String sqlQuery = "UPDATE DIRECTOR SET " +
                " NAME = ? " +
                " WHERE DIRECTOR_ID = ?";
        int linesChanged = jdbcTemplate.update(sqlQuery,
                director.getName(),
                director.getId());
        if (linesChanged > 0) {
            log.info("Обновлены данные режиссёра с id {} и именем {}", director.getId(), director.getName());
        } else {
            throw new NoSuchElementException("Режиссёр с id " + director.getId() + " не найден. Обновление отклонено.");
        }
        return director;
    }

    @Override
    public List<Director> someDirectors(List<Integer> directorsToInt) {
        NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        String sql = "SELECT DIRECTOR_ID, NAME FROM DIRECTOR WHERE DIRECTOR.DIRECTOR_ID IN (:values)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("values", directorsToInt);

        return namedTemplate.query(
                sql, parameters,
                (rs, rowNow) -> new Director(rs.getInt("DIRECTOR_ID"), rs.getString("NAME")));
    }

    @Override
    public void delete(int id) {
        String sqlDeletePairs = "DELETE FROM FILM_DIRECTOR WHERE DIRECTOR_ID = ?";
        jdbcTemplate.update(sqlDeletePairs, id);
        String sqlDeleteDirector = "DELETE FROM DIRECTOR WHERE DIRECTOR_ID = ?";
        int linesDeleted = jdbcTemplate.update(sqlDeleteDirector, id);
        if (linesDeleted == 0)
            throw new NoSuchElementException("Не найден режиссёр с айди " + id);
    }

    private List<Director> directorParsing(SqlRowSet dirRows) {
        List<Director> directors = new ArrayList<>();
        while (dirRows.next()) {
            Director director = new Director(
                    dirRows.getInt("DIRECTOR_ID"),
                    dirRows.getString("NAME"));
            directors.add(director);
        }
        return directors;
    }
}
