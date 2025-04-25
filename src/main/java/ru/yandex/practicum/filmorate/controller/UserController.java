package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение списка всех пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) throws ValidationException {
        log.info("Получен запрос на создание пользователя");

        validateUserFields(user);

        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, установлен логин по умолчанию");
        }

        users.put(user.getId(), user);
        log.info("Пользователь успешно создан");
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User updatedUser) throws NotFoundException, ValidationException {
        log.info("Получен запрос на обновление пользователя");
        User existingUser = users.get(updatedUser.getId());

        if (updatedUser.getId() == null) {
            throw new IllegalArgumentException("ID пользователя должен быть указан");
        }

        if (existingUser == null) {
            throw new NotFoundException("Пользователь с ID " + updatedUser.getId() + " не найден");
        }

        validateUserFields(updatedUser);

        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setLogin(updatedUser.getLogin());
        existingUser.setName(updatedUser.getName());
        existingUser.setBirthday(updatedUser.getBirthday());

        log.info("Пользователь успешно обновлен");
        return existingUser;
    }

    private void validateUserFields(User user) throws ValidationException {
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}