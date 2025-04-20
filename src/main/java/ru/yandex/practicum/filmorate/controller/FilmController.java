package ru.yandex.practicum.filmorate.controller;

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
    public Film create(@RequestBody Film film) throws ValidationException {
        log.info("Получен запрос на создание фильма");

        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Название фильма не может быть пустым");
            throw new ValidationException("Имя фильма должно быть заполнено");
        }
        if (film.getDescription().length() > 201) {
            log.error("Описание больше 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Попытка добавление фильма до 28 декабря 1895 года");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.error("Отрицательная продолжительность");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан");
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film updateFilm) throws ValidationException, NotFoundException {
        log.info("Получен запрос на обновление фильма");

        if (updateFilm.getId() == null) {
            log.error("Ошибка: ID фильма не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (films.containsKey(updateFilm.getId())) {
            Film oldFilm = films.get(updateFilm.getId());

            if (updateFilm.getName() == null || updateFilm.getName().isBlank()) {
                log.error("Название фильма не может быть пустым");
                throw new ValidationException("Имя фильма должно быть заполнено");
            }
            if (updateFilm.getDescription().length() > 201) {
                log.error("Описание больше 200 символов");
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            if (updateFilm.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
                log.error("Попытка добавление фильма до 28 декабря 1895 года");
                throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
            }
            if (updateFilm.getDuration() < 0) {
                log.error("Отрицательная продолжительность");
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }

            oldFilm.setName(updateFilm.getName());
            oldFilm.setDescription(updateFilm.getDescription());
            oldFilm.setReleaseDate(updateFilm.getReleaseDate());
            oldFilm.setDuration(updateFilm.getDuration());

            log.info("Фильм успешно обновлен");
            return oldFilm;
        }

        log.error("Фильм не найден");
        throw new NotFoundException("Фильм с id = " + updateFilm.getId() + " не найден");
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