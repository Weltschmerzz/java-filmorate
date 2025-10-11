package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;
    private final Map<Long, Map<Long, FriendshipStatus>> friendships = new HashMap<>();


    @Override
    public User create(User user) {
        long id = idCounter++;
        user.setId(id);
        users.put(id, user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();

        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException(String.format("Не найден пользователь с id: %s", id));
        }

        users.put(id, user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    @Override
    public Long deleteById(Long id) {
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException(String.format("Не найден пользователь с id: %s", id));
        }
        users.remove(id);
        log.info("Удалён пользователь id={}", id);
        return id;
    }

    @Override
    public User getById(Long id) {
        User user = users.get(id);

        if (user == null) {
            throw new NotFoundException(String.format("Не найден пользователь с id: %s", id));
        }

        return user;
    }

    @Override
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void setFriendConnection(Long from, Long to, FriendshipStatus status) {
        friendships
                .computeIfAbsent(from, k -> new HashMap<>())
                .put(to, status);
    }

    @Override
    public void removeFriendConnection(Long from, Long to) {
        Map<Long, FriendshipStatus> m = friendships.get(from);
        if (m != null) {
            m.remove(to);
            if (m.isEmpty()) friendships.remove(from);
        }
    }
}
