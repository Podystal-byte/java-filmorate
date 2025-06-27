package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("userDbStorage")
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        int userId = keyHolder.getKey().intValue();
        user.setId(userId);

        return findById(userId);
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        return findById(user.getId());
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", id);
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        if (users.isEmpty()) {
            return null;
        }
        User user = users.get(0);
        loadFriends(user);
        return user;
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);
        users.forEach(this::loadFriends);
        return users;
    }


    @Override
    public void sendFriendRequest(int senderId, int recipientId) {
        Boolean existsReverse = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM friendship WHERE user_id = ? AND friend_id = ?)", Boolean.class, recipientId, senderId);

        if (Boolean.TRUE.equals(existsReverse)) {
            jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id) VALUES (?, ?), (?, ?)", senderId, recipientId, recipientId, senderId);
        } else {
            jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)", senderId, recipientId);
        }
    }

    @Override
    public void acceptFriendRequest(int userId, int friendId) {
        String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";

        jdbcTemplate.update(sql, userId, friendId);

    }


    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Optional<User> findUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        return users.stream().findFirst();
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder().id(rs.getInt("user_id")).email(rs.getString("email")).login(rs.getString("login")).name(rs.getString("name")).birthday(rs.getDate("birthday").toLocalDate()).build();
    }

    private void loadFriends(User user) {
        int userId = user.getId();

        Set<Integer> outgoing = new HashSet<>(jdbcTemplate.queryForList("SELECT friend_id FROM friendship WHERE user_id = ?", Integer.class, userId));


        Set<Integer> incoming = new HashSet<>(jdbcTemplate.queryForList("SELECT user_id FROM friendship WHERE friend_id = ?", Integer.class, userId));


        Set<Integer> mutual = outgoing.stream().filter(incoming::contains).collect(Collectors.toSet());

        user.setFriends(mutual);
        user.setOutgoingFriendRequests(outgoing);
        user.setIncomingFriendRequests(incoming);
    }
}