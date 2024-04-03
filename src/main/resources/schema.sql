/*DROP TABLE IF EXISTS FILM_GENRES ;
DROP TABLE IF EXISTS FILM_DIRECTOR ;
DROP table IF EXISTS DIRECTOR ;
DROP TABLE IF EXISTS FRIENDS ;
DROP TABLE IF EXISTS LIKES ;
DROP TABLE IF EXISTS FILMS ;
DROP TABLE IF EXISTS GENRES ;
DROP TABLE IF EXISTS MPA;
DROP TABLE IF EXISTS USERS; */

CREATE TABLE IF NOT EXISTS PUBLIC.GENRES (
        GENRE_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        GENRE varchar(100),
        CONSTRAINT uq_genre UNIQUE (GENRE)
);

CREATE TABLE IF NOT EXISTS PUBLIC.MPA (
        MPA_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        MPA varchar(20),
        CONSTRAINT uq_mpa UNIQUE (MPA)
);

CREATE TABLE IF NOT EXISTS PUBLIC.FILMS (
        FILM_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        NAME varchar(100),
        DESCRIPTION varchar(200),
        RELEASE_DATE date,
        DURATION integer,
        MPA_ID INTEGER REFERENCES PUBLIC.MPA(MPA_ID) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS PUBLIC.FILM_GENRES (
        PAIR_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        FILM_ID INTEGER REFERENCES PUBLIC.FILMS(FILM_ID) ON DELETE CASCADE,
        GENRE_ID INTEGER REFERENCES PUBLIC.GENRES(GENRE_ID) ON DELETE RESTRICT,
        CONSTRAINT uq_film_genres UNIQUE (FILM_ID, GENRE_ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.USERS (
        USER_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        EMAIL varchar(100),
        LOGIN varchar(50),
        NAME varchar(50),
        BIRTHDAY date
);

CREATE TABLE IF NOT EXISTS PUBLIC.LIKES (
        LIKE_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        FILM_ID INTEGER REFERENCES PUBLIC.FILMS(FILM_ID) ON DELETE CASCADE,
        USER_ID INTEGER REFERENCES PUBLIC.USERS(USER_ID) ON DELETE CASCADE,
        CONSTRAINT uq_likes UNIQUE (FILM_ID, USER_ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.FRIENDS (
        FRIENDS_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        USER_ID INTEGER REFERENCES PUBLIC.USERS(USER_ID) ON DELETE CASCADE,
        FRIEND_ID INTEGER REFERENCES PUBLIC.USERS(USER_ID) ON DELETE CASCADE,
        CONSTRAINT uq_friends UNIQUE (USER_ID, FRIEND_ID)
);
CREATE TABLE IF NOT EXISTS PUBLIC.DIRECTOR(
        DIRECTOR_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        NAME varchar(50)
);
CREATE TABLE IF NOT EXISTS PUBLIC.FILM_DIRECTOR (
        PAIR_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        FILM_ID INTEGER REFERENCES Films(FILM_ID),
        DIRECTOR_ID INTEGER REFERENCES Director(DIRECTOR_ID)
);
