package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(int userId, int friendId) throws NotFoundException, ValidationException {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователи уже друзья");
        }

        // Добавляем только одностороннюю дружбу (user -> friend)
        userStorage.addFriend(userId, friendId);
        user.getFriends().add(friendId);

        log.info("Пользователь {} добавил пользователя {} в друзья", userId, friendId);
    }

    public List<User> getFriends(int userId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        System.out.println("userId=" + userId + ", friends=" + user.getFriends());
        return user.getFriends().stream()
                .map(id -> {
                    try {
                        return getUserOrThrow(id);
                    } catch (NotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public boolean removeFriend(int userId, int friendId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        friend.getFriends().remove(userId);
        user.getFriends().remove(friendId);
        userStorage.removeFriend(userId, friendId);

        boolean wasFriends = user.getFriends().remove(friendId);

        return wasFriends;
    }

    public List<User> getCommonFriends(int userId, int otherUserId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        User otherUser = getUserOrThrow(otherUserId);

        Set<Integer> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(otherUser.getFriends());

        return commonIds.stream()
                .map(id -> {
                    try {
                        return getUserOrThrow(id);
                    } catch (NotFoundException e) {
                        throw new RuntimeException("Общий друг с id=" + id + " не найден", e);
                    }
                })
                .collect(Collectors.toList());
    }

    public User getUserOrThrow(int id) throws NotFoundException {
        return Optional.ofNullable(userStorage.findById(id))
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User add(User user) {
        return userStorage.add(user);
    }

    public User update(User user) throws NotFoundException {
        if (user.getId() == null || userStorage.findById(user.getId()) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.update(user);
    }

    public void delete(int id) throws NotFoundException {
        User user = userStorage.findById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        userStorage.delete(id);
        log.info("Пользователь с id={} удалён", id);
    }
}