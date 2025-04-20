package ru.yandex.practicum.filmorate.controller;

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
    public User create(@RequestBody User user) throws ValidationException {
        log.info("Получен запрос на создание пользователя");

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

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
    public User update(@RequestBody User updateUser) throws ValidationException, NotFoundException {
        log.info("Получен запрос на обновление пользователя");

        if (updateUser.getId() == null) {
            log.error("Id пользователя не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(updateUser.getId())) {
            log.error("Пользователь с данным id не найден");
            throw new NotFoundException("Пользователь с id = " + updateUser.getId() + " не найден");
        }

        if (updateUser.getEmail() == null || updateUser.getEmail().isBlank() || !updateUser.getEmail().contains("@")) {
            log.error("Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }

        if (updateUser.getLogin() == null || updateUser.getLogin().isBlank() || updateUser.getLogin().contains(" ")) {
            log.error("Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (updateUser.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        User oldUser = users.get(updateUser.getId());
        oldUser.setEmail(updateUser.getEmail());
        oldUser.setLogin(updateUser.getLogin());
        oldUser.setName(updateUser.getName());
        oldUser.setBirthday(updateUser.getBirthday());

        log.info("Пользователь успешно обновлен");
        return oldUser;
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