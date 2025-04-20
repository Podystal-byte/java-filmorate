package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
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
    void createUserWithEmptyEmailShouldFail() {
        validUser.setEmail("");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void createUserWithInvalidEmailShouldFail() {
        validUser.setEmail("invalid-email");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void createUserWithEmptyLoginShouldFail() {
        validUser.setLogin("");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void createUserWithLoginContainingSpacesShouldFail() {
        validUser.setLogin("login with spaces");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void createUserWithFutureBirthdayShouldFail() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
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