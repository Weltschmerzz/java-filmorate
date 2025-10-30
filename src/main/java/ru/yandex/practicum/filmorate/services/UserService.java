package ru.yandex.practicum.filmorate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.DomainValidator;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final DomainValidator<User> validator = new UserValidator();

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        validator.validateCreate(user);
        User newUser = userStorage.create(user);

        log.info("Создан пользователь через сервис: {}", newUser);
        return newUser;
    }

    public User update(User newUser) {
        if (newUser == null) {
            log.info("Тело запроса не должно быть пустым!");
            throw new ValidationException("Тело запроса не должно быть пустым!");
        }
        Long id = newUser.getId();
        if (id == null) {
            log.info("id должен быть указан!");
            throw new ValidationException("id должен быть указан!");
        }
        User existing = userStorage.getById(newUser.getId());
        if (existing == null) {
            log.info(String.format("Не найден пользователь с id: %d", id));
            throw new NotFoundException(String.format("Не найден пользователь с id: %d", id));
        }

        validator.validateUpdate(newUser);

        if (newUser.getLogin() != null) {
            existing.setLogin(newUser.getLogin());
        }
        if (newUser.getEmail() != null) {
            existing.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            if (newUser.getName().isBlank()) {
                existing.setName(existing.getLogin());
            } else {
                existing.setName(newUser.getName());
            }
        }
        if (newUser.getBirthday() != null) {
            existing.setBirthday(newUser.getBirthday());
        }

        log.info("Обновлён пользователь через сервис: {}", existing);
        return userStorage.update(existing);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }


    public User getUserById(Long id) {
        if (id == null) {
            throw new ValidationException("id должен быть указан");
        }
        User user = userStorage.getById(id);
        if (Objects.isNull(user)) {
            throw new NotFoundException(String.format("Не найден пользователь с id: %d", id));
        }
        log.info("Запрос на вывод пользователя по id выполнен: {}", user);
        return user;
    }

    public void deleteById(Long id) {
        log.info("Запрос на удаление пользователя по id: {}", id);
        userStorage.deleteById(id);
    }

    public void addFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        boolean update1 = user.getFriends().add(friendId);
        boolean update2 = friend.getFriends().add(userId);

        if (update1) {
            userStorage.setFriendConnection(userId, friendId, FriendshipStatus.CONFIRMED);
            userStorage.update(user);
        }
        if (update2) {
            userStorage.setFriendConnection(friendId, userId, FriendshipStatus.CONFIRMED);
            userStorage.update(friend);
        }
        log.info("Пользователь {}, добавил в друзья пользователя {}", user.getId(), friend.getId());
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }

        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        boolean update1 = user.getFriends().remove(friendId);
        boolean update2 = friend.getFriends().remove(userId);

        if (update1) {
            userStorage.removeFriendConnection(userId, friendId);
            userStorage.update(user);
        }
        if (update2) {
            userStorage.removeFriendConnection(friendId, userId);
            userStorage.update(friend);
        }
        log.info("Пользователь {}, удалил из друзей пользователя {}", user.getId(), friend.getId());
    }

    public List<User> getFriends(Long userId) {
        log.info("Запрос на вывод всех друзей пользователя {}", userId);
        return userStorage.getById(userId).getFriends()
                .stream()
                .map(userStorage::getById)
                .toList();
    }

    public List<User> getCommonFriends(Long userid, Long otherUserId) {
        User user = userStorage.getById(userid);
        User otherUser = userStorage.getById(otherUserId);

        Set<Long> commonFriends = new HashSet<>(user.getFriends());
        commonFriends.retainAll(otherUser.getFriends());

        if (commonFriends.isEmpty()) {
            throw new NotFoundException("У пользователей нет общих друзей!");
        }

        log.info("Запрос на вывод общих друзей пользователя {} и {}", userid, otherUserId);
        return commonFriends.stream()
                .map(userStorage::getById)
                .toList();
    }
}

