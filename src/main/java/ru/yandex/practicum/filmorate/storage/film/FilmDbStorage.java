package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;

    private static final String SQL_SELECT_FILM_BASE = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   m.id AS mpa_id, m.name AS mpa_name
            FROM films f
            JOIN mpa m ON m.id = f.mpa_id
            """;

    private static final String SQL_FIND_ALL_FILMS = SQL_SELECT_FILM_BASE + " ORDER BY f.id";

    private static final String SQL_FIND_FILM_BY_ID = SQL_SELECT_FILM_BASE + " WHERE f.id = ?";

    private static final String SQL_LOAD_LIKES_BY_FILM_ID = """
                SELECT user_id FROM film_likes WHERE film_id = ?
            """;

    private static final String SQL_LOAD_GENRES_BY_FILM_ID = """
                SELECT g.id AS genre_id, g.name AS genre_name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
            """;

    private static final String SQL_UPDATE_FILM = """
            UPDATE films
               SET name = ?,
                   description = ?,
                   release_date = ?,
                   duration = ?,
                   mpa_id = ?,
                   updated_at = CURRENT_TIMESTAMP
             WHERE id = ?
            """;

    private static final String SQL_DELETE_FILM_BY_ID = "DELETE FROM films WHERE id = ?";

    private static final String SQL_DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String SQL_MERGE_FILM_GENRE = """
            MERGE INTO film_genres (film_id, genre_id)
            KEY (film_id, genre_id)
            VALUES (?, ?)
            """;

    private static final String SQL_GET_GENRE_BY_ID = "SELECT id, name FROM genres WHERE id = ?";
    private static final String SQL_SELECT_ALL_GENRES = "SELECT id, name FROM genres ORDER BY id";
    private static final String SQL_COUNT_GENRE_BY_ID = "SELECT COUNT(*) FROM genres WHERE id = ?";

    private static final String SQL_GET_MPA_BY_ID = "SELECT id, name FROM mpa WHERE id = ?";
    private static final String SQL_SELECT_ALL_MPA = "SELECT id, name FROM mpa ORDER BY id";
    private static final String SQL_COUNT_MPA_BY_ID = "SELECT COUNT(*) FROM mpa WHERE id = ?";

    private static final String SQL_DELETE_FILM_LIKES =
            "DELETE FROM film_likes WHERE film_id = ?";

    private static final String SQL_MERGE_FILM_LIKE = """
            MERGE INTO film_likes (film_id, user_id)
            KEY (film_id, user_id)
            VALUES (?, ?)
            """;

    //CRUD

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(SQL_FIND_ALL_FILMS, filmRowMapper);
        if (films.isEmpty()) return films;

        for (Film film : films) {
            Long id = film.getId();

            // лайки
            jdbc.query(SQL_LOAD_LIKES_BY_FILM_ID,
                    (rs, rn) -> {
                        film.getLikes().add(rs.getLong("user_id"));
                        return null;
                    },
                    id);

            // жанры
            jdbc.query(SQL_LOAD_GENRES_BY_FILM_ID,
                    (rs, rn) -> {
                        film.getGenres().add(new Genre(rs.getLong("genre_id"), rs.getString("genre_name")));
                        return null;
                    },
                    id);
        }
        return films;
    }


    @Override
    public Film getById(Long id) {
        Film film = jdbc.query(SQL_FIND_FILM_BY_ID, filmRowMapper, id).stream().findFirst().orElseThrow(() -> new NotFoundException("Фильм id=" + id + " не найден"));

        List<Long> likeUserIds = jdbc.query(SQL_LOAD_LIKES_BY_FILM_ID, (rs, rn) -> rs.getLong("user_id"), id);
        film.getLikes().addAll(likeUserIds);

        List<Genre> genres = jdbc.query(SQL_LOAD_GENRES_BY_FILM_ID, (rs, rn) -> new Genre(rs.getLong("genre_id"), rs.getString("genre_name")), id);
        film.setGenres(new LinkedHashSet<>(genres));

        return film;
    }

    @Override
    @Transactional
    public Film create(Film film) {
        Long mpaId = (film.getMpa() != null && film.getMpa().getId() != null)
                ? film.getMpa().getId()
                : 1L;

        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                .withTableName("films")
                .usingGeneratedKeyColumns("id")
                .usingColumns(
                        "name",
                        "description",
                        "release_date",
                        "duration",
                        "mpa_id"
                );

        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("mpa_id", mpaId);

        Number key = insert.executeAndReturnKey(params);
        film.setId(key.longValue());

        upsertGenres(film);
        upsertLikes(film);

        return getById(film.getId());
    }

    @Override
    @Transactional
    public Film update(Film film) {
        Long mpaId = (film.getMpa() != null && film.getMpa().getId() != null) ? film.getMpa().getId() : 1L;

        int updated = jdbc.update(SQL_UPDATE_FILM, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), mpaId, film.getId());
        if (updated == 0) {
            throw new NotFoundException("Film id=" + film.getId() + " not found");
        }

        jdbc.update(SQL_DELETE_FILM_GENRES, film.getId());
        upsertGenres(film);
        upsertLikes(film);
        return getById(film.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jdbc.update(SQL_DELETE_FILM_GENRES, id);
        jdbc.update(SQL_DELETE_FILM_BY_ID, id);
    }

    //GENRES / MPA

    @Override
    public Genre getGenreById(Long id) {
        List<Genre> list = jdbc.query(SQL_GET_GENRE_BY_ID, (rs, rn) -> new Genre(rs.getLong("id"), rs.getString("name")), id);
        return list.stream().findFirst().orElseThrow(() -> new NotFoundException("Genre id=" + id + " not found"));
    }

    @Override
    public Map<Long, Genre> getAllGenres() {
        return jdbc.query(SQL_SELECT_ALL_GENRES, (rs, rn) -> new Genre(rs.getLong("id"), rs.getString("name"))).stream().collect(Collectors.toMap(Genre::getId, g -> g, (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public boolean isGenreExist(Long id) {
        Integer cnt = jdbc.queryForObject(SQL_COUNT_GENRE_BY_ID, Integer.class, id);
        return cnt != null && cnt > 0;
    }

    @Override
    public Mpa getMpaById(Long id) {
        List<Mpa> list = jdbc.query(SQL_GET_MPA_BY_ID, (rs, rn) -> new Mpa(rs.getLong("id"), rs.getString("name")), id);
        return list.stream().findFirst().orElseThrow(() -> new NotFoundException("MPA id=" + id + " not found"));
    }

    @Override
    public Map<Long, Mpa> getAllMpa() {
        return jdbc.query(SQL_SELECT_ALL_MPA, (rs, rn) -> new Mpa(rs.getLong("id"), rs.getString("name"))).stream().collect(Collectors.toMap(Mpa::getId, m -> m, (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public boolean isMpaExist(Long id) {
        Integer cnt = jdbc.queryForObject(SQL_COUNT_MPA_BY_ID, Integer.class, id);
        return cnt != null && cnt > 0;
    }

    //helpers
    private void upsertGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        for (Genre g : film.getGenres()) {
            jdbc.update(SQL_MERGE_FILM_GENRE, film.getId(), g.getId());
        }
    }

    private void upsertLikes(Film film) {
        if (film.getLikes() == null) return;
        jdbc.update(SQL_DELETE_FILM_LIKES, film.getId());
        for (Long uid : film.getLikes()) {
            jdbc.update(SQL_MERGE_FILM_LIKE, film.getId(), uid);
        }
    }
}
