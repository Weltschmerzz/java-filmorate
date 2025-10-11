package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Long deleteById(Long id);

    User getById(Long id);

    Collection<User> findAll();

    void setFriendConnection(Long from, Long to, FriendshipStatus status);

    void removeFriendConnection(Long from, Long to);
}
