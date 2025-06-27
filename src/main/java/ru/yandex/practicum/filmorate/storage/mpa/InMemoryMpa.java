package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

public class InMemoryMpa implements MpaStorage {
    @Override
    public List<Mpa> getAllMpaRatings() {
        return List.of();
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        return Optional.empty();
    }
}
