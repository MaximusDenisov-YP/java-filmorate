CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email    VARCHAR(50) NOT NULL UNIQUE,
    login    VARCHAR(30) NOT NULL UNIQUE,
    name     VARCHAR(30),
    birthday DATE
);

CREATE TABLE IF NOT EXISTS friendships
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id_from  BIGINT,
    user_id_to    BIGINT,
    friend_status VARCHAR(20) CHECK (friend_status IN ('REQUESTED', 'ACCEPTED', 'REJECTED')),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id_from) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id_to) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (user_id_from, user_id_to)
);

CREATE TABLE IF NOT EXISTS genres
(
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa_ratings
(
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS directors
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS films
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    release_date DATE,
    duration     BIGINT NOT NULL,
    mpa_rating   INT
    FOREIGN KEY (mpa_rating) REFERENCES mpa_ratings (id)
);

CREATE TABLE IF NOT EXISTS film_directors (
    film_id BIGINT REFERENCES films(id) ON DELETE CASCADE,
    director_id BIGINT REFERENCES directors(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, director_id)
    );


CREATE TABLE IF NOT EXISTS films_likes
(
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id BIGINT,
    user_id BIGINT,
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS films_genres
(
    film_id  BIGINT,
    genre_id INT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres (id)
);

CREATE TABLE IF NOT EXISTS reviews
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    content     VARCHAR(1000),
    is_positive BOOLEAN,
    user_id     BIGINT,
    film_id     BIGINT,
    useful      BIGINT,
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews_likes
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    review_id   BIGINT,
    user_id     BIGINT,
    is_positive BOOLEAN,
    FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (review_id, user_id)
);