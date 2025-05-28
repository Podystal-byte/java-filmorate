package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) throws ValidationException {
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) throws NotFoundException, ValidationException {
        if (film.getId() == null || filmStorage.findById(film.getId()) == null) {
            throw new NotFoundException("Фильм не найден");
        }
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.update(film);
    }


    public void addLike(int filmId, int userId) throws ValidationException, NotFoundException {
        Film film = getFilmOrThrow(filmId);
        User user = getUserOrThrow(userId);

        if (!film.getLikes().add(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }
        filmStorage.update(film);
    }

    public void removeLike(int filmId, int userId) throws ValidationException, NotFoundException {
        Film film = getFilmOrThrow(filmId);
        User user = getUserOrThrow(userId);

        if (!film.getLikes().remove(userId)) {
            throw new ValidationException("Лайк не найден");
        }
        filmStorage.update(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream().sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size())).limit(count).collect(Collectors.toList());
    }

    public Film getFilmOrThrow(int id) throws NotFoundException {
        return Optional.ofNullable(filmStorage.findById(id)).orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    public User getUserOrThrow(int id) throws NotFoundException {
        return Optional.ofNullable(userStorage.findById(id)).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public void delete(int id) {
        filmStorage.delete(id);
    }

    private void validateReleaseDate(LocalDate releaseDate) throws ValidationException {
        if (releaseDate.isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }
}

