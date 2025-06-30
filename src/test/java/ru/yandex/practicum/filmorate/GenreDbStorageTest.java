package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM genre");
        jdbcTemplate.update("INSERT INTO genre (id, name) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм')");
    }

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreStorage.getAllGenres();
        assertEquals(3, genres.size());
        assertEquals("Комедия", genres.get(0).getName());
    }

    @Test
    void testGetGenreById() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(2);
        assertTrue(genreOptional.isPresent());
        assertEquals("Драма", genreOptional.get().getName());
    }

    @Test
    void testGetNonExistingGenre() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(999);
        assertTrue(genreOptional.isEmpty());
    }
}