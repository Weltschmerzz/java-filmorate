package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper rm = new UserRowMapper();

    private static final String INSERT_SQL = """
            INSERT INTO users (email, login, name, birthday, created_at, updated_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

    private static final String UPDATE_SQL = """
            UPDATE users
               SET email = ?,
                   login = ?,
                   name = ?,
                   birthday = ?,
                   updated_at = CURRENT_TIMESTAMP
             WHERE id = ?
            """;

    private static final String DELETE_SQL = "DELETE FROM users WHERE id = ?";


    private static final String SELECT_BY_ID_SQL = """
            SELECT id, email, login, name, birthday
              FROM users
             WHERE id = ?
            """;

    private static final String SELECT_ALL_SQL = """
            SELECT id, email, login, name, birthday
              FROM users
             ORDER BY id
            """;

    private static final String SELECT_FRIEND_IDS = """
            SELECT friend_id FROM friendships
             WHERE user_id = ?
            """;

    private static final String UPSERT_FRIEND = """
            MERGE INTO friendships (user_id, friend_id, status_id)
            KEY (user_id, friend_id)
            VALUES (?, ?, ?)
            """;

    private static final String DELETE_FRIEND = """
            DELETE FROM friendships
             WHERE user_id = ? AND friend_id = ?
            """;

    private static final int STATUS_CONFIRMED_ID = 1;

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_SQL, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        int updated = jdbc.update(UPDATE_SQL, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (updated == 0) {
            throw new NotFoundException("Не найден пользователь с id: " + user.getId());
        }
        return user;
    }

    @Override
    public Long deleteById(Long id) {
        int n = jdbc.update(DELETE_SQL, id);
        if (n == 0) {
            throw new NotFoundException("Не найден пользователь с id: " + id);
        }
        return id;
    }

    @Override
    public User getById(Long id) {
        Optional<User> opt = jdbc.query(SELECT_BY_ID_SQL, rm, id).stream().findFirst();
        if (opt.isEmpty()) {
            throw new NotFoundException("Не найден пользователь с id: " + id);
        }
        User u = opt.get();

        List<Long> friendIds = jdbc.query(SELECT_FRIEND_IDS, (rs, rn) -> rs.getLong(1), id);
        u.getFriends().clear();
        u.getFriends().addAll(friendIds);
        return u;
    }

    @Override
    public java.util.Collection<User> findAll() {
        return jdbc.query(SELECT_ALL_SQL, rm);
    }

    @Override
    public void setFriendConnection(Long from, Long to, FriendshipStatus status) {
        jdbc.update(UPSERT_FRIEND, from, to, STATUS_CONFIRMED_ID);
    }

    @Override
    public void removeFriendConnection(Long from, Long to) {
        jdbc.update(DELETE_FRIEND, from, to);
    }

}
