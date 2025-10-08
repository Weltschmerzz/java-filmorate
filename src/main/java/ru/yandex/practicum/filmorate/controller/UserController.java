package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.services.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        return userService.update(newUser);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable
                       @Positive(message = "id пользователя должен быть > 0")
                       Long id) {
        userService.deleteById(id);
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable
                          @Positive(message = "id пользователя должен быть > 0")
                          Long id,
                          @PathVariable
                          @Positive(message = "id друга должен быть > 0")
                          Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable
                             @Positive(message = "id пользователя должен быть > 0")
                             Long id,
                             @PathVariable
                             @Positive(message = "id друга должен быть > 0")
                             Long friendId) {
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable
                                 @Positive(message = "id пользователя должен быть > 0")
                                 Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable
                                       @Positive(message = "id пользователя должен быть > 0")
                                       Long id,
                                       @PathVariable
                                       @Positive(message = "id пользователя должен быть > 0")
                                       Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}