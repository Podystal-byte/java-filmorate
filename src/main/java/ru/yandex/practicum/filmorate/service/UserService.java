package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(int acceptorId, int requesterId) throws NotFoundException {
        User acceptor = getUserOrThrow(acceptorId);
        User requester = getUserOrThrow(requesterId);

        if (!acceptor.getIncomingFriendRequests().contains(requesterId)) {
            throw new NotFoundException("Запрос на дружбу не найден");
        }

        userStorage.acceptFriendRequest(acceptorId, requesterId);

        User updatedAcceptor = userStorage.findById(acceptorId);
        User updatedRequester = userStorage.findById(requesterId);

        acceptor.setIncomingFriendRequests(updatedAcceptor.getIncomingFriendRequests());
        acceptor.setFriends(updatedAcceptor.getFriends());
        requester.setOutgoingFriendRequests(updatedRequester.getOutgoingFriendRequests());
        requester.setFriends(updatedRequester.getFriends());

        acceptor.getIncomingFriendRequests().remove(requesterId);
        requester.getOutgoingFriendRequests().remove(acceptorId);
    }


    public void friendShipOffer(int senderId, int recipientId) throws ValidationException, NotFoundException {
        User sender = getUserOrThrow(senderId);
        User recipient = getUserOrThrow(recipientId);

        if (sender.getFriends().contains(recipientId)) {
            throw new ValidationException("Пользователи уже друзья");
        }

        if (sender.getOutgoingFriendRequests().contains(recipientId)) {
            throw new ValidationException("Заявка уже отправлена");
        }

        userStorage.sendFriendRequest(senderId, recipientId);

        User updatedSender = userStorage.findById(senderId);
        User updatedRecipient = userStorage.findById(recipientId);

        sender.setOutgoingFriendRequests(updatedSender.getOutgoingFriendRequests());
        recipient.setIncomingFriendRequests(updatedRecipient.getIncomingFriendRequests());
    }


    public void removeFriend(int userId, int friendId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (!user.getFriends().contains(friendId)) {
            throw new NotFoundException("Друг не найден");
        }

        userStorage.removeFriend(userId, friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public List<User> getFriends(int userId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        return user.getFriends().stream().map(userStorage::findById).collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherUserId) throws NotFoundException {
        User user = getUserOrThrow(userId);
        User otherUser = getUserOrThrow(otherUserId);

        return user.getFriends().stream()
                .filter(id -> otherUser.getFriends().contains(id))
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

