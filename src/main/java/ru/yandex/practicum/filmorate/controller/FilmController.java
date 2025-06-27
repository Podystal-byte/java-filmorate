package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на список всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable int id) throws NotFoundException {
        log.info("Получен запрос на фильм с ID " + id);
        return filmService.getFilmOrThrow(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ValidationException, NotFoundException {
        log.info("Получен запрос на создание фильма");
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) throws ValidationException, NotFoundException {
        log.info("Получен запрос на обновление фильма с ID " + film.getId());
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Получен запрос на удаление фильма с ID " + id);
        filmService.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) throws ValidationException, NotFoundException {
        log.info("Пользователь " + userId + " ставит лайк фильму " + id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) throws ValidationException, NotFoundException {
        log.info("Пользователь " + userId + " удаляет лайк с фильма " + id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на " + count + " популярных фильмов");
        return filmService.getPopularFilms(count);
    }
}
