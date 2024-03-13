CREATE TABLE IF NOT EXISTS PUBLIC.GENRES (
        GENRE_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        GENRE varchar(20),
        CONSTRAINT uq_genre UNIQUE (GENRE)
);

CREATE TABLE IF NOT EXISTS PUBLIC.MPA (
        MPA_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        MPA varchar(5),
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
