package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM users");

        testUser1 = User.builder()
                .email("user1@mail.ru")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        testUser2 = User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
    }

    @Test
    void testAddUser() {
        User addedUser = userStorage.add(testUser1);
        assertNotNull(addedUser.getId());
        assertEquals(testUser1.getEmail(), addedUser.getEmail());
    }

    @Test
    void testDeleteUser() {
        User addedUser = userStorage.add(testUser1);
        userStorage.delete(addedUser.getId());
        assertNull(userStorage.findById(addedUser.getId()));
    }

    @Test
    void testFindAllUsers() {
        userStorage.add(testUser1);
        userStorage.add(testUser2);
        Collection<User> users = userStorage.findAll();
        assertEquals(2, users.size());
    }
}