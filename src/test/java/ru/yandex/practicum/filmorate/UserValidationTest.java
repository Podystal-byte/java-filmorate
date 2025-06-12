package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userService);

        validUser = new User();
        validUser.setEmail("isr@gmail.com");
        validUser.setLogin("ivan");
        validUser.setName("Ivan123");
        validUser.setBirthday(LocalDate.of(2001, 1, 13));
    }

    @Test
    void createValidUserShouldSucceed() {
        assertDoesNotThrow(() -> userController.create(validUser));
    }

    @Test
    void createUserWithEmptyNameShouldUseLoginAsName() throws ValidationException {
        validUser.setName("");
        User createdUser = userController.create(validUser);
        assertEquals(validUser.getLogin(), createdUser.getName());
    }

    @Test
    void createUserWithNullNameShouldUseLoginAsName() throws ValidationException {
        validUser.setName(null);
        User createdUser = userController.create(validUser);
        assertEquals(validUser.getLogin(), createdUser.getName());
    }

    @Test
    void updateUserWithInvalidIdShouldFail() {
        validUser.setId(999);
        assertThrows(NotFoundException.class, () -> userController.update(validUser));
    }
}
