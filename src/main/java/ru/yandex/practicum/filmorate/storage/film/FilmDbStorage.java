package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film getFilm(int id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(commonPartOfQuery() + " WHERE f.FILM_ID = ?", id);
        List<Film> oneFilm = filmsParsing(filmRows);
        if (oneFilm.size() == 1) {
            return oneFilm.get(0);
        } else {
            log.info("Фильм с идентификатором {} не найден.", id);
            throw new NoSuchElementException("Фильм с id " + id + " не найден.");
        }
    }

    @Override
    public List<Film> findAll() {
        SqlRowSet allFilmsRows = jdbcTemplate.queryForRowSet(commonPartOfQuery() + " ORDER BY FILM_ID");
        return filmsParsing(allFilmsRows);
    }

    /**
     Скриншот форматированного (для лучшей читаемости) запроса приведён в файле FILMS_WITH_GENRES в папке resources.
     Выборки, получаемые при выполнении вложенных запросов (см. выделение) - в файлах PARTIAL_EXECUTION 1, 2 и 3.
    */
    private String commonPartOfQuery() {
        return "SELECT f.FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, m.MPA_ID, m.MPA, " +
                "GENRES_FOR_PARSING FROM FILMS f LEFT JOIN (SELECT GROUP_CONCAT(ID_AND_GENRE SEPARATOR ';') AS " +
                "GENRES_FOR_PARSING, FILM_ID FROM (SELECT CONCAT_WS(',',GENRE_ID,GENRE) AS ID_AND_GENRE, FILM_ID " +
                "FROM (SELECT fg.FILM_ID, fg.GENRE_ID, g.GENRE FROM GENRES g JOIN FILM_GENRES fg ON fg.GENRE_ID = " +
                "g.GENRE_ID)) GROUP BY FILM_ID) GENRES_IN_GROUP ON f.FILM_ID = GENRES_IN_GROUP.FILM_ID JOIN MPA m " +
                "ON m.MPA_ID = f.MPA_ID";
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        SqlRowSet popularFilmsRows = jdbcTemplate.queryForRowSet(commonPartOfQuery() +
                " LEFT JOIN (SELECT l.FILM_ID, COUNT(l.USER_ID) POPULARITY FROM LIKES l GROUP BY " +
                "l.FILM_ID) AS POPULAR_FILMS ON f.FILM_ID = POPULAR_FILMS.FILM_ID " +
                "ORDER BY POPULAR_FILMS.POPULARITY DESC LIMIT ?", count);
        return filmsParsing(popularFilmsRows);
    }

    private List<Film> filmsParsing(SqlRowSet filmsRows) {
        List<Film> films = new ArrayList<>();
        while (filmsRows.next()) {
            int mpaId = Integer.parseInt(Objects.requireNonNull(filmsRows.getString("MPA_ID")));
            String mpaName = filmsRows.getString("MPA");
            Mpa mpa = new Mpa(mpaId, mpaName);
            String rowOfGenres = filmsRows.getString("GENRES_FOR_PARSING");
            List<Genre> genres = new ArrayList<>();
            if (rowOfGenres != null) genres = genresParsing(rowOfGenres);
            Film film = new Film(
                    Integer.parseInt(Objects.requireNonNull(filmsRows.getString("FILM_ID"))),
                    filmsRows.getString("NAME"),
                    filmsRows.getString("DESCRIPTION"),
                    LocalDate.parse(Objects.requireNonNull(filmsRows.getString("RELEASE_DATE"))),
                    Integer.parseInt(Objects.requireNonNull(filmsRows.getString("DURATION"))),
                    mpa, genres);
            films.add(film);
        }
        return films;
    }

    private List<Genre> genresParsing(String rowOfGenres) {
        List<Genre> genres = new ArrayList<>();
        String delimiter = ";";
        String[] content = rowOfGenres.split(delimiter);
        for (String line : content) {
            String divider = ",";
            String[] pair = line.split(divider);
            Genre genre = new Genre(Integer.parseInt(pair[0]), pair[1]);
            genres.add(genre);
        }
        return genres;
    }

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");
        film.setId(simpleJdbcInsert.executeAndReturnKey(film.filmToMap()).intValue());
        if (film.getGenres() != null) {
            addGenres(film);
        }
        log.info("Добавлен новый фильм с id {}", film.getId());
        return film;
    }

    private void addGenres(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILM_GENRES")
                .usingGeneratedKeyColumns("PAIR_ID");
        int filmId = film.getId();
        List<Integer> genresInInt = film.genresToInt();
        for (int genre : genresInInt) {
            simpleJdbcInsert.executeAndReturnKey(genreToMap(filmId, genre)).intValue();
        }
    }

    private Map<String, Integer> genreToMap(int filmId, int genre) {
        Map<String, Integer> values = new HashMap<>();
        values.put("FILM_ID", filmId);
        values.put("GENRE_ID", genre);
        return values;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == 0) {
            create(film);
        } else {
            String sqlQuery = "UPDATE FILMS SET " +
                    "NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, " +
                    "DURATION = ?, MPA_ID = ? WHERE FILM_ID = ?";
            int linesChanged = jdbcTemplate.update(sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId());
            if (film.getGenres() != null) {
                String deleteGenresQuery = "DELETE FROM FILM_GENRES WHERE FILM_ID = ?";
                jdbcTemplate.update(deleteGenresQuery, film.getId());
                addGenres(film);
            }
            if (linesChanged > 0) {
                log.info("Обновлены данные фильма с id {}", film.getId());
            } else {
                throw new NoSuchElementException("Фильм с id " + film.getId() + " не найден. Обновление отклонено.");
            }
        }
        return film;
    }

    @Override
    public Map<String, String> addLike(int id, int userId) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("LIKES")
                .usingGeneratedKeyColumns("LIKE_ID");
        int returningKey = simpleJdbcInsert.executeAndReturnKey(likesToMap(id, userId)).intValue();
        if (returningKey > 0) {
            log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, id);
            return Map.of("result", "Пользователь с id " + userId + " поставил лайк фильму с id " + id);
        } else {
            throw new NoSuchElementException("Пользователь с id " + userId + " или фильм с id " + id +
                    " не найдены. Добавление лайка отклонено.");
        }
    }

    private Map<String, Integer> likesToMap(int id, int userId) {
        Map<String, Integer> values = new HashMap<>();
        values.put("FILM_ID", id);
        values.put("USER_ID", userId);
        return values;
    }

    @Override
    public Map<String, String> removeLike(int id, int userId) {
        String sqlQuery = "DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?";
        int linesDelete = jdbcTemplate.update(sqlQuery, id, userId);
        if (linesDelete > 0) {
            log.info("Пользователь с id {} удалил лайк фильму с id {}", userId, id);
            return Map.of("result", "Пользователь с id " + userId + " удалил лайк фильму с id " + id);
        } else {
            throw new NoSuchElementException("Сведения о лайке от пользователя с id " + userId + " фильму с id " + id
                    + " не найдены. Удаление лайка отклонено.");
        }
    }
}
