package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaStorage;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM mpa");
        jdbcTemplate.update("INSERT INTO mpa (id, name) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13')");
    }

    @Test
    void testGetAllMpaRatings() {
        List<Mpa> mpaRatings = mpaStorage.getAllMpaRatings();
        assertEquals(3, mpaRatings.size());
        assertEquals("G", mpaRatings.get(0).getName());
    }

    @Test
    void testGetMpaById() {
        Optional<Mpa> mpaOptional = mpaStorage.getMpaById(2);
        assertTrue(mpaOptional.isPresent());
        assertEquals("PG", mpaOptional.get().getName());
    }

    @Test
    void testGetNonExistingMpa() {
        Optional<Mpa> mpaOptional = mpaStorage.getMpaById(999);
        assertTrue(mpaOptional.isEmpty());
    }
}