package ru.yandex.practicum.filmorate.storage.film;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    void delete(int id);

    Film findById(int id);

    Collection<Film> findAll();
}

