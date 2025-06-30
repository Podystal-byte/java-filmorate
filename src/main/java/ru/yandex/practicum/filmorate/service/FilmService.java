package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage, GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Film addFilm(Film film) throws ValidationException, NotFoundException {
        validateReleaseDate(film.getReleaseDate());
        if (film.getMpa() == null) {
            throw new ValidationException("Не указан MPA рейтинг");
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genreStorage.getGenreById(genre.getId()).isEmpty()) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) throws NotFoundException, ValidationException {
        if (film.getId() == null || filmStorage.findById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.update(film);
    }


    public void addLike(int filmId, int userId) throws NotFoundException, ValidationException {
        Film film = getFilmOrThrow(filmId);
        User user = getUserOrThrow(userId);

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк");
        }

        filmStorage.addLike(filmId, userId);
        film.getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) throws NotFoundException, ValidationException {
        Film film = getFilmOrThrow(filmId);
        User user = getUserOrThrow(userId);

        if (!film.getLikes().contains(userId)) {
            throw new ValidationException("Лайк не найден");
        }

        filmStorage.removeLike(filmId, userId);
        film.getLikes().remove(userId);
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

