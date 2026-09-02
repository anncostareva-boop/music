package com.music.app.dao.interfaces;

import com.music.app.entity.Genre;
import com.music.app.entity.User;
import com.music.app.exception.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

public interface IUserDAO {

    void addUser(User user) throws DataAccessException;
    List<User> getAllUsers() throws SQLException;
    User findUserById(Long UserId) throws SQLException;
    User findUserByName(String name) throws DataAccessException;
    void editProfile(User user) throws SQLException;
    User findUserByEmail(String email) throws SQLException;
    User findUserByPhone(String phone) throws SQLException;
    Long getUserIdByUsername(String name);
}
