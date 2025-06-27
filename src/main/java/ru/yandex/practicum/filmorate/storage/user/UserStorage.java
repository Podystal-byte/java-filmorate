package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User add(User user);

    User update(User user);

    void delete(int id);

    User findById(int id);

    Collection<User> findAll();

    void sendFriendRequest(int senderId, int recipientId);

    void acceptFriendRequest(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    Optional<User> findUserById(int id);
}

