package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(int userId, int friendId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public void removeFriend(int userId, int friendId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public List<User> getFriends(int userId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        return user.getFriends().stream().map(userStorage::findById).collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherUserId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        User otherUser = getUserOrThrow(otherUserId);

        return user.getFriends().stream().filter(id -> otherUser.getFriends().contains(id)).map(userStorage::findById).collect(Collectors.toList());
    }

    public User getUserOrThrow(int id) throws NotFoundException {
        return Optional.ofNullable(userStorage.findById(id)).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
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


    public void delete(int id) {
        userStorage.delete(id);
    }
}

