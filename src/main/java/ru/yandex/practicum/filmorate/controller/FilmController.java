package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на получение списка всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Получен запрос на создание фильма");
        validateReleaseDate(film.getReleaseDate());

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан");
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) throws NotFoundException, ValidationException {
        log.info("Получен запрос на обновление фильма");

        if (updatedFilm.getId() == null) {
            throw new IllegalArgumentException("ID фильма должен быть указан");
        }

        Film oldFilm = films.get(updatedFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм с ID " + updatedFilm.getId() + " не найден");
        }

        validateReleaseDate(updatedFilm.getReleaseDate());

        oldFilm.setName(updatedFilm.getName());
        oldFilm.setDescription(updatedFilm.getDescription());
        oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
        oldFilm.setDuration(updatedFilm.getDuration());

        log.info("Фильм успешно обновлен");
        return oldFilm;
    }

    private void validateReleaseDate(LocalDate releaseDate) throws ValidationException {
        if (releaseDate.isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        int nextId = ++currentMaxId;
        log.debug("Сгенерирован новый ID для фильма: {}", nextId);
        return nextId;
    }
}