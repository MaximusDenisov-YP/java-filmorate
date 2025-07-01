CREATE TABLE IF NOT EXISTS films
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    release_date DATE,
    duration     INT,
    genre        VARCHAR(20) CHECK (genre IN ('ACTION', 'COMEDY', 'DRAMA', 'HORROR', 'SCI_FI')),
    mpa_rating   VARCHAR(5) CHECK (mpa_rating IN ('G', 'PG', 'PG_13', 'R', 'NC_17'))
);

CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email    VARCHAR(255) NOT NULL UNIQUE,
    login    VARCHAR(255) NOT NULL UNIQUE,
    name     VARCHAR(255),
    birthday DATE
);

CREATE TABLE IF NOT EXISTS likes
(
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id BIGINT,
    user_id BIGINT,
    FOREIGN KEY (film_id) REFERENCES FILMS (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
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