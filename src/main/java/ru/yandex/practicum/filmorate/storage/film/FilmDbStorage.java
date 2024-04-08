package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.*;

import static ru.yandex.practicum.filmorate.model.Constants.*;

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
     * Скриншот форматированного (для лучшей читаемости) запроса приведён в файле FILMS_WITH_GENRES в папке resources.
     * Выборки, получаемые при выполнении вложенных запросов (см. выделение) - в файлах PARTIAL_EXECUTION 1, 2 и 3.
     */
    private String commonPartOfQuery(boolean withGenre) {
        String genreString = withGenre ? " JOIN FILM_GENRES fg1 WHERE fg1.GENRE_ID = ? " : "";
        String joinString = withGenre ? "RIGHT" : "LEFT";
        return "SELECT f.FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, m.MPA_ID, m.MPA, " +
                "GENRES_FOR_PARSING, DIRECTORS_FOR_PARSING FROM FILMS f " + joinString + " JOIN (SELECT GROUP_CONCAT(ID_AND_GENRE SEPARATOR ';') AS " +
                "GENRES_FOR_PARSING, FILM_ID FROM (SELECT CONCAT_WS(',',GENRE_ID,GENRE) AS ID_AND_GENRE, FILM_ID " +
                "FROM (SELECT fg.FILM_ID, fg.GENRE_ID, g.GENRE FROM GENRES g JOIN FILM_GENRES fg ON fg.GENRE_ID = " +
                "g.GENRE_ID" + genreString + ")) GROUP BY FILM_ID) GENRES_IN_GROUP ON f.FILM_ID = GENRES_IN_GROUP.FILM_ID " +
                "LEFT JOIN (SELECT GROUP_CONCAT(ID_AND_DIRECTOR SEPARATOR ';') AS DIRECTORS_FOR_PARSING, " +
                "FILM_ID FROM (SELECT CONCAT_WS(',', DIRECTOR_ID, NAME) AS ID_AND_DIRECTOR, " +
                "FILM_ID FROM (SELECT fd.FILM_ID, fd.DIRECTOR_ID, d.NAME FROM DIRECTOR d JOIN FILM_DIRECTOR fd " +
                " ON fd.DIRECTOR_ID = d.DIRECTOR_ID))" +
                "GROUP BY FILM_ID) DIRECTORS_IN_GROUP ON f.FILM_ID  = DIRECTORS_IN_GROUP.FILM_ID " +
                "JOIN MPA m ON m.MPA_ID = f.MPA_ID ";
    }

    private String commonPartOfQuery() {
        return commonPartOfQuery(false);
    }

    @Override
    public List<Film> getPopularFilms(int count, int genre, int year) {
        SqlRowSet popularFilmsRows = null;
        String tail = " LEFT JOIN (SELECT l.FILM_ID, COUNT(l.USER_ID) POPULARITY FROM LIKES l GROUP BY " +
                "l.FILM_ID) AS POPULAR_FILMS ON f.FILM_ID = POPULAR_FILMS.FILM_ID " +
                "ORDER BY POPULAR_FILMS.POPULARITY DESC LIMIT ?";
        String tailWithYear = " LEFT JOIN (SELECT l.FILM_ID, COUNT(l.USER_ID) POPULARITY FROM LIKES l GROUP BY " +
                "l.FILM_ID) AS POPULAR_FILMS ON f.FILM_ID = POPULAR_FILMS.FILM_ID " +
                "WHERE EXTRACT (YEAR FROM f.RELEASE_DATE) = ? " +
                "ORDER BY POPULAR_FILMS.POPULARITY DESC LIMIT ?";
        if (genre == 0 && year == 0) {
            popularFilmsRows = jdbcTemplate.queryForRowSet(commonPartOfQuery(false) + tail, count);
        } else if (genre == 0) {
            popularFilmsRows = jdbcTemplate.queryForRowSet(commonPartOfQuery(false) + tailWithYear, year, count);
        } else if (year == 0) {
            popularFilmsRows = jdbcTemplate.queryForRowSet(commonPartOfQuery(true) + tail, genre, count);
        } else {
            popularFilmsRows = jdbcTemplate.queryForRowSet(commonPartOfQuery(true) + tailWithYear,
                    genre, year, count);
        }
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
            String rowOfDirectors = filmsRows.getString("DIRECTORS_FOR_PARSING");//SHTEFAN добавление режиссёров
            List<Director> directors = new ArrayList<>(); //SHTEFAN добавление режиссёров
            if (rowOfGenres != null) genres = genresParsing(rowOfGenres);
            if (rowOfDirectors != null) directors = directorsParsing(rowOfDirectors);//SHTEFAN добавление режиссёров
            Film film = new Film(
                    filmsRows.getInt("FILM_ID"),
                    filmsRows.getString("NAME"),
                    filmsRows.getString("DESCRIPTION"),
                    LocalDate.parse(Objects.requireNonNull(filmsRows.getString("RELEASE_DATE"))),
                    Integer.parseInt(Objects.requireNonNull(filmsRows.getString("DURATION"))),
                    mpa, genres, directors);//SHTEFAN добавление режиссёров
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

    private List<Director> directorsParsing(String rowOfDirectors) {
        //SHTEFAN добавление режиссёров
        List<Director> directors = new ArrayList<>();
        String delimiter = ";";
        String[] content = rowOfDirectors.split(delimiter);
        for (String line : content) {
            String divider = ",";
            String[] pair = line.split(divider);
            Director director = new Director(Integer.parseInt(pair[0]), pair[1]);
            directors.add(director);
        }
        return directors;
    }

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");
        film.setId(simpleJdbcInsert.executeAndReturnKey(film.filmToMap()).intValue());
        if (film.getGenres() != null) {
            addGenres(film);
        } else {
            film.setGenres(new ArrayList<>());
        }
        if (film.getDirectors() != null) {
            //SHTEFAN добавление режиссёров
            addDirectors(film);
        } else {
            film.setDirectors(new ArrayList<>());
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

    private void addDirectors(Film film) {
        //SHTEFAN добавление режиссёров
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILM_DIRECTOR")
                .usingGeneratedKeyColumns("PAIR_ID");
        int filmId = film.getId();
        List<Integer> directorInInt = film.directorsToInt();
        for (int director : directorInInt) {
            simpleJdbcInsert.executeAndReturnKey(directorToMap(filmId, director)).intValue();
        }
    }

    private Map<String, Integer> genreToMap(int filmId, int genre) {
        Map<String, Integer> values = new HashMap<>();
        values.put("FILM_ID", filmId);
        values.put("GENRE_ID", genre);
        return values;
    }

    private Map<String, Integer> directorToMap(int filmId, int directorId) {
        //SHTEFAN добавление режиссёров
        Map<String, Integer> values = new HashMap<>();
        values.put("FILM_ID", filmId);
        values.put("DIRECTOR_ID", directorId);
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
            //SHTEFAN добавление режиссёров
            String deleteGenresQuery = "DELETE FROM FILM_GENRES WHERE FILM_ID = ?";
            jdbcTemplate.update(deleteGenresQuery, film.getId());
            if (film.getGenres() != null) {
                addGenres(film);
            } else {
                film.setGenres(new ArrayList<>());
            }
            String deleteDirectorsQuery = "DELETE FROM FILM_DIRECTOR WHERE FILM_ID = ?";
            jdbcTemplate.update(deleteDirectorsQuery, film.getId());
            if (film.getDirectors() != null) {
                addDirectors(film);
            } else {
                film.setDirectors(new ArrayList<>());
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
            addEvent(userId, id, ID_ADD);
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
            addEvent(userId, id, ID_REMOVE);
            log.info("Пользователь с id {} удалил лайк фильму с id {}", userId, id);
            return Map.of("result", "Пользователь с id " + userId + " удалил лайк фильму с id " + id);
        } else {
            throw new NoSuchElementException("Сведения о лайке от пользователя с id " + userId + " фильму с id " + id
                    + " не найдены. Удаление лайка отклонено.");
        }
    }

    @Override
    public void delete(int filmId) {
        String sqlFilmDirector = "DELETE FROM FILM_DIRECTOR WHERE FILM_ID = ? ;";
        jdbcTemplate.update(sqlFilmDirector, filmId);
        String sqlFilms = "DELETE FROM FILMS WHERE FILM_ID = ? ;";
        int linesDelete = jdbcTemplate.update(sqlFilms, filmId);
        if (linesDelete > 0) {
            log.info("Фильм с id {} удален", filmId);
        } else {
            throw new NoSuchElementException("Ошибка при удалении. Фильм с id " + filmId + " не найден.");
        }
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String commonIds = "SELECT l1.FILM_ID FROM LIKES l1 JOIN LIKES l2 ON l1.FILM_ID = l2.FILM_ID WHERE l1.USER_ID = ? AND l2.USER_ID = ?";
        SqlRowSet commonFilmsRows = jdbcTemplate.queryForRowSet(
                "SELECT RESULT.FILM_ID, RESULT.NAME, RESULT.DESCRIPTION, RESULT.RELEASE_DATE, RESULT.DURATION, RESULT.MPA_ID, RESULT.MPA, RESULT.GENRES_FOR_PARSING, RESULT.DIRECTORS_FOR_PARSING FROM (" +
                        commonPartOfQuery() + ") AS RESULT WHERE RESULT.FILM_ID IN (" + commonIds + ");", userId, friendId
        );
        return filmsParsing(commonFilmsRows);
    }

    @Override
    public List<Film> getRecommendedFilms(Integer id) {
        String rowSortFilms = "SELECT FILM_ID FROM LIKES WHERE FILM_ID NOT IN (SELECT FILM_ID FROM LIKES WHERE USER_ID = ?) GROUP BY FILM_ID ORDER BY COUNT(FILM_ID) DESC";
        SqlRowSet recommendedFilmsRows = jdbcTemplate.queryForRowSet(
                "SELECT RESULT.FILM_ID, RESULT.NAME, RESULT.DESCRIPTION, RESULT.RELEASE_DATE, RESULT.DURATION, RESULT.MPA_ID, RESULT.MPA, RESULT.GENRES_FOR_PARSING, RESULT.DIRECTORS_FOR_PARSING FROM (" +
                        commonPartOfQuery() + ") AS RESULT WHERE RESULT.FILM_ID IN (" + rowSortFilms + ");", id
        );
        return filmsParsing(recommendedFilmsRows);
    }


    @Override
    public List<Film> getByDirector(int id, String sortBy) {
        //SHTEFAN Поиск по режиссёру
        SqlRowSet directorFilmsRows = jdbcTemplate.queryForRowSet(commonPartOfQuery() +
                " LEFT JOIN (SELECT l.FILM_ID, COUNT(l.USER_ID) POPULARITY FROM LIKES l GROUP BY " +
                "l.FILM_ID) AS POPULAR_FILMS ON f.FILM_ID = POPULAR_FILMS.FILM_ID " +
                "where f.FILM_ID IN (SELECT fd2.FILM_ID FROM FILM_DIRECTOR fd2 WHERE fd2.DIRECTOR_ID = ?) " +
                "ORDER BY " + sortBy, id);
        return filmsParsing(directorFilmsRows);
    }

    private void addEvent(int userId, int entityId, int operationId) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("EVENTS")
                .usingGeneratedKeyColumns("EVENT_ID");
        simpleJdbcInsert.executeAndReturnKey(Event.eventToMap(userId, entityId, ID_LIKE, operationId)).intValue();
    }
}
