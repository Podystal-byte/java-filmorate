package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
class FilmorateApplicationTests {

    @Autowired
    private UserDbStorage userStorage;
    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private MpaDbStorage mpaStorage;
    @Autowired
    private GenreDbStorage genreStorage;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser1;
    private User testUser2;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM genre");
        jdbcTemplate.update("DELETE FROM mpa");

        jdbcTemplate.update("INSERT INTO mpa (id, name) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13')");
        jdbcTemplate.update("INSERT INTO genre (id, name) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм')");

        testUser1 = User.builder()
                .email("user1@mail.ru")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        testUser2 = User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new Mpa("G", 1))
                .genres(Set.of(new Genre(1, "Комедия")))
                .build();
    }

    @Test
    void testAddUser_ShouldReturnUserWithId() {
        User addedUser = userStorage.add(testUser1);
        assertNotNull(addedUser.getId());
        assertEquals(testUser1.getEmail(), addedUser.getEmail());
    }

    @Test
    void testDeleteUser_ShouldRemoveUserFromDb() {
        User addedUser = userStorage.add(testUser1);
        userStorage.delete(addedUser.getId());
        assertNull(userStorage.findById(addedUser.getId()));
    }

    @Test
    void testFindUserById_ShouldReturnCorrectUser() {
        User addedUser = userStorage.add(testUser1);
        User foundUser = userStorage.findById(addedUser.getId());
        assertEquals(addedUser.getId(), foundUser.getId());
    }

    @Test
    void testFindAllUsers_ShouldReturnAllAddedUsers() {
        userStorage.add(testUser1);
        userStorage.add(testUser2);
        Collection<User> users = userStorage.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void testSendFriendRequest_ShouldCreateOneSidedFriendship() {
        User user1 = userStorage.add(testUser1);
        User user2 = userStorage.add(testUser2);

        userStorage.sendFriendRequest(user1.getId(), user2.getId());

        User updatedUser1 = userStorage.findById(user1.getId());
        assertTrue(updatedUser1.getOutgoingFriendRequests().contains(user2.getId()));
        assertFalse(updatedUser1.getFriends().contains(user2.getId()));
    }

    @Test
    void testAcceptFriendRequest_ShouldCreateMutualFriendship() {
        User user1 = userStorage.add(testUser1);
        User user2 = userStorage.add(testUser2);

        userStorage.sendFriendRequest(user1.getId(), user2.getId());
        userStorage.acceptFriendRequest(user2.getId(), user1.getId());

        User updatedUser1 = userStorage.findById(user1.getId());
        User updatedUser2 = userStorage.findById(user2.getId());

        assertTrue(updatedUser1.getFriends().contains(user2.getId()));
        assertTrue(updatedUser2.getFriends().contains(user1.getId()));
    }

    @Test
    void testRemoveFriend_ShouldRemoveFriendship() {
        User user1 = userStorage.add(testUser1);
        User user2 = userStorage.add(testUser2);

        userStorage.sendFriendRequest(user1.getId(), user2.getId());
        userStorage.acceptFriendRequest(user2.getId(), user1.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());

        User updatedUser1 = userStorage.findById(user1.getId());
        assertFalse(updatedUser1.getFriends().contains(user2.getId()));
    }

    @Test
    void testAddFilm_ShouldReturnFilmWithId() {
        Film addedFilm = filmStorage.add(testFilm);
        assertNotNull(addedFilm.getId());
        assertEquals(testFilm.getName(), addedFilm.getName());
    }


    @Test
    void testDeleteFilm_ShouldRemoveFilmFromDb() {
        Film addedFilm = filmStorage.add(testFilm);
        filmStorage.delete(addedFilm.getId());
        assertNull(filmStorage.findById(addedFilm.getId()));
    }

    @Test
    void testFindFilmById_ShouldReturnCorrectFilm() {
        Film addedFilm = filmStorage.add(testFilm);
        Film foundFilm = filmStorage.findById(addedFilm.getId());
        assertEquals(addedFilm.getId(), foundFilm.getId());
        assertEquals(1, foundFilm.getGenres().size());
    }

    @Test
    void testFindAllFilms_ShouldReturnAllAddedFilms() {
        filmStorage.add(testFilm);
        Collection<Film> films = filmStorage.findAll();
        assertEquals(1, films.size());
    }

    @Test
    void testAddLike_ShouldAddLikeToFilm() {
        User user = userStorage.add(testUser1);
        Film film = filmStorage.add(testFilm);

        filmStorage.addLike(film.getId(), user.getId());
        Film updatedFilm = filmStorage.findById(film.getId());

        assertTrue(updatedFilm.getLikes().contains(user.getId()));
    }

    @Test
    void testRemoveLike_ShouldRemoveLikeFromFilm() {
        User user = userStorage.add(testUser1);
        Film film = filmStorage.add(testFilm);

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());
        Film updatedFilm = filmStorage.findById(film.getId());

        assertFalse(updatedFilm.getLikes().contains(user.getId()));
    }

    @Test
    void testGetLikes_ShouldReturnAllLikesForFilm() {
        User user1 = userStorage.add(testUser1);
        User user2 = userStorage.add(testUser2);
        Film film = filmStorage.add(testFilm);

        filmStorage.addLike(film.getId(), user1.getId());
        filmStorage.addLike(film.getId(), user2.getId());

        Set<Integer> likes = filmStorage.getLikes(film.getId());
        assertEquals(2, likes.size());
        assertTrue(likes.containsAll(Set.of(user1.getId(), user2.getId())));
    }

    @Test
    void testGetAllMpaRatings_ShouldReturnAllMpa() {
        List<Mpa> mpaRatings = mpaStorage.getAllMpaRatings();

        assertThat(mpaRatings).hasSize(3);
        assertThat(mpaRatings)
                .extracting(Mpa::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13");
    }

    @Test
    void testGetMpaById_WithExistingId_ShouldReturnMpa() {
        Optional<Mpa> mpaOptional = mpaStorage.getMpaById(2);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa.getId()).isEqualTo(2);
                    assertThat(mpa.getName()).isEqualTo("PG");
                });
    }

    @Test
    void testGetMpaById_WithNonExistingId_ShouldReturnEmpty() {
        Optional<Mpa> mpaOptional = mpaStorage.getMpaById(999);

        assertThat(mpaOptional).isEmpty();
    }

    @Test
    void testGetAllGenres_ShouldReturnAllGenresInOrder() {
        List<Genre> genres = genreStorage.getAllGenres();

        assertThat(genres).hasSize(3);
        assertThat(genres)
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм");
    }

    @Test
    void testGetGenreById_WithExistingId_ShouldReturnGenre() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(1);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1);
                    assertThat(genre.getName()).isEqualTo("Комедия");
                });
    }

    @Test
    void testGetGenreById_WithNonExistingId_ShouldReturnEmpty() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(999);

        assertThat(genreOptional).isEmpty();
    }
}