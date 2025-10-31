-- Справочники
CREATE TABLE IF NOT EXISTS mpa
(
    id   INT PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS genres
(
    id   INT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS friend_status
(
    id   INT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);

-- Основные таблицы
CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    login      VARCHAR(100) NOT NULL UNIQUE,
    name       VARCHAR(255),
    birthday   DATE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at TIMESTAMP    NULL
);

CREATE TABLE IF NOT EXISTS films
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(1500),
    release_date DATE         NOT NULL,
    duration     INT          NOT NULL CHECK (duration > 0),
    mpa_id       INT          NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at   TIMESTAMP,
    CONSTRAINT fk_films_mpa FOREIGN KEY (mpa_id) REFERENCES mpa (id)
);

-- Промежуточные таблицы
CREATE TABLE IF NOT EXISTS film_genres
(
    film_id  BIGINT NOT NULL,
    genre_id INT    NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_fg_film FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    CONSTRAINT fk_fg_genre FOREIGN KEY (genre_id) REFERENCES genres (id)
);

CREATE TABLE IF NOT EXISTS film_likes
(
    film_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (film_id, user_id),
    CONSTRAINT fk_fl_film FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    CONSTRAINT fk_fl_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS friendships
(
    user_id    BIGINT NOT NULL,
    friend_id  BIGINT NOT NULL,
    status_id  INT    NOT NULL DEFAULT 1,
    created_at TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT fk_fr_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_fr_friend FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_fr_status FOREIGN KEY (status_id) REFERENCES friend_status (id),
    CONSTRAINT ck_self_friend CHECK (user_id <> friend_id)
);
