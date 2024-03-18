package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Component
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> allMPA() {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM MPA ORDER BY MPA_ID");
        return mpaParsing(mpaRows);
    }

    @Override
    public Mpa getMPA(int id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM MPA WHERE MPA_ID = ?", id);
        List<Mpa> oneMPA = mpaParsing(mpaRows);
        if (oneMPA.size() == 1) {
            return oneMPA.get(0);
        } else {
            log.info("Возрастной рейтинг с идентификатором {} не найден.", id);
            throw new NoSuchElementException("Возрастной рейтинг с id " + id + " не найден.");
        }
    }

    private List<Mpa> mpaParsing(SqlRowSet mpaRows) {
        List<Mpa> mpaCatalog = new ArrayList<>();
        while (mpaRows.next()) {
            Mpa mpa = new Mpa(
                    Integer.parseInt(Objects.requireNonNull(mpaRows.getString("MPA_ID"))),
                    mpaRows.getString("MPA"));
            mpaCatalog.add(mpa);
        }
        return mpaCatalog;
    }
}
