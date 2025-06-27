package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public class InMemoryGenryStorage implements GenreStorage {
    @Override
    public List<Genre> getAllGenres() {
        return List.of();
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        return Optional.empty();
    }
}
