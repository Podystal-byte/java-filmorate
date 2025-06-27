package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на список всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable int id) throws NotFoundException {
        log.info("Получен запрос на пользователя с ID " + id);
        return userService.getUserOrThrow(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя");
        return userService.add(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) throws NotFoundException {
        log.info("Получен запрос на обновление пользователя с ID " + user.getId());
        return userService.update(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Получен запрос на удаление пользователя с ID " + id);
        userService.delete(id);
    }

    @PutMapping("/{userId}/friends/request/{friendId}")
    public void sendFriendRequest(@PathVariable int userId, @PathVariable int friendId) throws NotFoundException, ValidationException {
        log.info("Запрос на добавление в друзья от пользователя: " + friendId);
        userService.friendShipOffer(userId, friendId);
    }

    @PutMapping("/{userId}/friends/accept/{friendId}")
    public void acceptFriendRequest(@PathVariable int userId, @PathVariable int friendId) throws NotFoundException {
        log.info("Запрос на добавление в друзья от пользователя: " + friendId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable int id, @PathVariable int friendId) throws NotFoundException {
        log.info("Пользователь " + id + " удаляет из друзей пользователя " + friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) throws NotFoundException {
        log.info("Получен запрос на список друзей пользователя с ID " + id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) throws NotFoundException {
        log.info("Получен запрос на список общих друзей пользователей " + id + " и " + otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
