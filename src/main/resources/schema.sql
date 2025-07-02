CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email    VARCHAR(255) NOT NULL UNIQUE,
    login    VARCHAR(255) NOT NULL UNIQUE,
    name     VARCHAR(255),
    birthday DATE
);

CREATE TABLE IF NOT EXISTS friendship
(
    friendship_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id_from  BIGINT,
    user_id_to    BIGINT,
    friend_status VARCHAR(20) CHECK (friend_status IN ('REQUESTED', 'ACCEPTED', 'REJECTED')),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id_from) REFERENCES users (id),
    FOREIGN KEY (user_id_to) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS genres
(
    id   INT PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa_ratings
(
    id   INT PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS films
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         VARCHAR NOT NULL,
    description  TEXT,
    release_date DATE,
    duration     BIGINT  NOT NULL,
    mpa_rating   INT
--     CONSTRAINT fk_mpa_rating FOREIGN KEY (mpa_rating) REFERENCES mpa_ratings (id)
);

CREATE TABLE IF NOT EXISTS likes
(
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id BIGINT,
    user_id BIGINT,
    FOREIGN KEY (film_id) REFERENCES FILMS (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS film_genres
(
    film_id  BIGINT,
    genre_id INT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films (id),
    FOREIGN KEY (genre_id) REFERENCES genres (id)
);