package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private UserDbStorage userStorage;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM genre");
        jdbcTemplate.update("DELETE FROM mpa");

        jdbcTemplate.update("INSERT INTO mpa (id, name) VALUES (1, 'G')");
        jdbcTemplate.update("INSERT INTO genre (id, name) VALUES (1, 'Комедия')");

        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new Mpa("G", 1))
                .genres(Set.of(new Genre(1, "Комедия")))
                .build();
    }

    @Test
    void testAddFilm() {
        Film addedFilm = filmStorage.add(testFilm);
        assertNotNull(addedFilm.getId());
        assertEquals(testFilm.getName(), addedFilm.getName());
    }

    @Test
    void testDeleteFilm() {
        Film addedFilm = filmStorage.add(testFilm);
        filmStorage.delete(addedFilm.getId());
        assertNull(filmStorage.findById(addedFilm.getId()));
    }

    @Test
    void testFindAllFilms() {
        filmStorage.add(testFilm);
        Collection<Film> films = filmStorage.findAll();
        assertEquals(1, films.size());
    }

    @Test
    void testLikeOperations() {
        User testUser = User.builder()
                .email("user@mail.ru")
                .login("user")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User addedUser = userStorage.add(testUser);
        Film addedFilm = filmStorage.add(testFilm);

        filmStorage.addLike(addedFilm.getId(), addedUser.getId());
        assertTrue(filmStorage.findById(addedFilm.getId()).getLikes().contains(addedUser.getId()));

        filmStorage.removeLike(addedFilm.getId(), addedUser.getId());
        assertFalse(filmStorage.findById(addedFilm.getId()).getLikes().contains(addedUser.getId()));
    }
}