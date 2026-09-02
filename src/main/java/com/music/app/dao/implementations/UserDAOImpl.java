package com.music.app.dao.implementations;

import com.music.app.dao.interfaces.ConnectionManager;
import com.music.app.dao.interfaces.IUserDAO;
import com.music.app.enums.Role;
import com.music.app.entity.User;
import com.music.app.exception.DataAccessException;
import com.music.app.exception.DataNotFoundException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDAOImpl implements IUserDAO {

    private final ConnectionManager cm;

    public UserDAOImpl(ConnectionManager cm) {
        this.cm = cm;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("phone"),
                Role.valueOf(rs.getString("role"))
        );
    }

    @Override
    public void addUser(User user) {
        String sql = """
                INSERT INTO users
                (username, email, password, phone, role)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = cm.getConnection(false, Connection.TRANSACTION_READ_COMMITTED);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getRole().name());

            ps.executeUpdate();


            con.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Couldn't add user.", e);
        }
    }

    @Override
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT *
                FROM users
                """;

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't load users.", e);
        }

        return users;
    }

    @Override
    public User findUserById(Long userId) {

        String sql = """
                SELECT *
                FROM users
                WHERE id = ?
                """;

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapUser(rs);
                }

                throw new DataNotFoundException(
                        "User with id " + userId + " not found."
                );
            }

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't find user by ID.", e);
        }
    }

    @Override
    public User findUserByName(String username) {

        String sql = """
                SELECT *
                FROM users
                WHERE username = ?
                """;

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapUser(rs);
                }

                throw new DataNotFoundException(
                        "User '" + username + "' not found."
                );
            }

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't find user by username.", e);
        }
    }

    @Override
    public void editProfile(User user) {

        String sql = """
                UPDATE users
                SET username = ?,
                    email = ?,
                    phone = ?
                WHERE id = ?
                """;

        try (Connection con = cm.getConnection(false, Connection.TRANSACTION_READ_COMMITTED);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhoneNumber());
            ps.setLong(4, user.getUserId());

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated == 0) {
                throw new DataNotFoundException(
                        "User with id " + user.getUserId() + " not found."
                );
            }

            con.commit();

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't edit profile.", e);
        }
    }

    @Override
    public User findUserByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs); // User found!
                }
            }
            // If the code reaches here, rs.next() was false (no user found with that phone)
            // Returning null is standard practice for DAOs when an entity isn't found
            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't find user by phone.", e);
        }
    }

    @Override
    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs); // User found!
                }
            }
            // If the code reaches here, rs.next() was false (no user found with that phone)
            // Returning null is standard practice for DAOs when an entity isn't found
            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't find user by email.", e);
        }

    }

    @Override
    public Long getUserIdByUsername(String name) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                } else {
                    throw new DataAccessException("User not found with username: " + name);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't find user by username.", e);
        }
    }
}
