package com.music.app.service.interfaces;

import com.music.app.entity.User;
import com.music.app.exception.DataAccessException;

import java.sql.SQLException;
import java.util.List;

public interface UserService {

    User getUserByName(String name) throws SQLException;
    User getUserById(Long userId) throws SQLException;
    void editProfile (User user) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    void addUser(User user) throws DataAccessException;
    Long getUserIdByUsername(String name);
}
