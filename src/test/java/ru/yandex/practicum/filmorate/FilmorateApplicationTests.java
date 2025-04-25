package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {
    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Валидный фильм");
        validFilm.setDescription("Правильное описание");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void createValidFilmShouldSucceed() {
        assertDoesNotThrow(() -> filmController.create(validFilm));
    }

    @Test
    void createFilmWithEarlyReleaseDateShouldFail() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(validFilm));
        assertEquals("Дата релиза должна быть не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void updateFilmWithInvalidIdShouldFail() {
        validFilm.setId(999);
        assertThrows(NotFoundException.class, () -> filmController.update(validFilm));
    }
}
