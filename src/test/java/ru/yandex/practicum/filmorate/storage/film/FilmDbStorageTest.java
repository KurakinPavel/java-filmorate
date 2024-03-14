package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.IdContainer;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    void getFilmAndCreateFilmTest() {
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        IdContainer mpa1 = new IdContainer();
        IdContainer genre11 = new IdContainer();
        IdContainer genre12 = new IdContainer();
        mpa1.setId(3);
        genre11.setId(1);
        genre12.setId(2);
        List<IdContainer> genres1 = new ArrayList<>();
        genres1.add(genre11);
        genres1.add(genre12);
        Film newFilm = new Film(0, "Film1", "film is film of film great film 1",
                LocalDate.of(1990, 1, 1), 120, mpa1, genres1);
        filmStorage.create(newFilm);
        Film savedFilm = filmStorage.getFilm(newFilm.getId());
        Assertions.assertEquals(savedFilm, newFilm, "Созданный и извлечённый объекты не совпадают");
    }

    @Test
    void findAll() {
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        IdContainer mpa1 = new IdContainer();
        IdContainer genre11 = new IdContainer();
        IdContainer genre12 = new IdContainer();
        mpa1.setId(3);
        genre11.setId(1);
        genre12.setId(2);
        List<IdContainer> genres1 = new ArrayList<>();
        genres1.add(genre11);
        genres1.add(genre12);
        Film newFilm1 = new Film(0, "Film1", "film is film of film great film 1",
                LocalDate.of(1990, 1, 1), 120, mpa1, genres1);
        filmStorage.create(newFilm1);

        IdContainer mpa2 = new IdContainer();
        IdContainer genre21 = new IdContainer();
        IdContainer genre22 = new IdContainer();
        mpa2.setId(2);
        genre21.setId(3);
        genre22.setId(4);
        List<IdContainer> genres2 = new ArrayList<>();
        genres2.add(genre21);
        genres2.add(genre22);
        Film newFilm2 = new Film(0, "Film2", "film 2 is film of 2 film great film 2",
                LocalDate.of(1995, 2, 4), 110, mpa2, genres2);
        filmStorage.create(newFilm2);

        List<Film> films = filmStorage.findAll();
        Assertions.assertEquals(2, films.size(), "Количество полученных из базы объектов "
                + "не совпадает с ожидаемым");
    }

    @Test
    void addAndRemoveLikeAndGetPopularFilmsTest() {
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User newUser1 = new User(0,"user1@email.ru", "vanya1123", "IIvan Petrov",
                LocalDate.of(1990, 1, 1));
        User newUser2 = new User(0,"user2@email.ru", "vanya1223", "IIIvan Petrov",
                LocalDate.of(1990, 2, 1));
        User newUser3 = new User(0,"user3@email.ru", "vanya1323", "IIIIvan Petrov",
                LocalDate.of(1990, 3, 1));
        userStorage.create(newUser1);
        int userId1 = newUser1.getId();
        userStorage.create(newUser2);
        int userId2 = newUser2.getId();
        userStorage.create(newUser3);
        int userId3 = newUser3.getId();

        IdContainer mpa1 = new IdContainer();
        IdContainer genre11 = new IdContainer();
        IdContainer genre12 = new IdContainer();
        mpa1.setId(3);
        genre11.setId(1);
        genre12.setId(2);
        List<IdContainer> genres1 = new ArrayList<>();
        genres1.add(genre11);
        genres1.add(genre12);
        Film newFilm1 = new Film(0, "Film1", "film is film of film great film 1",
                LocalDate.of(1990, 1, 1), 120, mpa1, genres1);
        filmStorage.create(newFilm1);
        int filmId1 = newFilm1.getId();

        IdContainer mpa2 = new IdContainer();
        IdContainer genre21 = new IdContainer();
        IdContainer genre22 = new IdContainer();
        mpa2.setId(2);
        genre21.setId(3);
        genre22.setId(4);
        List<IdContainer> genres2 = new ArrayList<>();
        genres2.add(genre21);
        genres2.add(genre22);
        Film newFilm2 = new Film(0, "Film2", "film 2 is film of 2 film great film 2",
                LocalDate.of(1995, 2, 4), 110, mpa2, genres2);
        filmStorage.create(newFilm2);
        int filmId2 = newFilm2.getId();

        Film newFilm3 = new Film(0, "Film3", "film 3 is 3 film of 3 film great 3 film 3",
                LocalDate.of(1999, 3, 5), 100, mpa2, genres2);
        filmStorage.create(newFilm3);
        int filmId3 = newFilm3.getId();

        filmStorage.addLike(filmId2, userId1);
        filmStorage.addLike(filmId2, userId2);
        filmStorage.addLike(filmId2, userId3);
        filmStorage.addLike(filmId3, userId2);
        filmStorage.addLike(filmId3, userId3);
        filmStorage.addLike(filmId1, userId1);

        List<Film> popularFilms1 = filmStorage.getPopularFilms(2);
        Assertions.assertEquals(2, popularFilms1.size(), "Количество полученных фильмов "
                + "отличается от ожидаемого");
        Assertions.assertEquals(2, popularFilms1.get(0).getId(), "Ожидался другой наиболее "
                + "популярный фильм");
        Assertions.assertEquals(3, popularFilms1.get(1).getId(), "Ожидался другой второй по "
                + "популярности фильм");

        filmStorage.removeLike(filmId2, userId1);
        filmStorage.removeLike(filmId2, userId2);
        filmStorage.removeLike(filmId3, userId2);
        filmStorage.removeLike(filmId3, userId3);
        filmStorage.addLike(filmId1, userId2);

        List<Film> popularFilms2 = filmStorage.getPopularFilms(2);
        Assertions.assertEquals(1, popularFilms2.get(0).getId(), "Ожидался другой наиболее "
                + "популярный фильм");
        Assertions.assertEquals(2, popularFilms2.get(1).getId(), "Ожидался другой второй по "
                + "популярности фильм");

    }

    @Test
    void updateTest() {
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        IdContainer mpa1 = new IdContainer();
        IdContainer genre11 = new IdContainer();
        IdContainer genre12 = new IdContainer();
        mpa1.setId(3);
        genre11.setId(1);
        genre12.setId(2);
        List<IdContainer> genres1 = new ArrayList<>();
        genres1.add(genre11);
        genres1.add(genre12);
        Film newFilm1 = new Film(0, "Film1", "film is film of film great film 1",
                LocalDate.of(1990, 1, 1), 120, mpa1, genres1);
        filmStorage.create(newFilm1);
        int id = newFilm1.getId();

        IdContainer mpa2 = new IdContainer();
        IdContainer genre21 = new IdContainer();
        IdContainer genre22 = new IdContainer();
        mpa2.setId(2);
        genre21.setId(3);
        genre22.setId(4);
        List<IdContainer> genres2 = new ArrayList<>();
        genres2.add(genre21);
        genres2.add(genre22);
        Film newFilm2 = new Film(id, "Film2", "film 2 is film of 2 film great film 2",
                LocalDate.of(1995, 2, 4), 110, mpa2, genres2);

        filmStorage.update(newFilm2);
        Film updatedFilm = filmStorage.getFilm(id);
        Assertions.assertEquals(updatedFilm, newFilm2, "Созданный и обновлённый объекты не совпадают");
    }
}
