package com.music.app.service.implementations;

import com.music.app.dao.interfaces.IUserDAO;
import com.music.app.entity.User;
import com.music.app.service.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final IUserDAO userDAO;

    public UserServiceImpl(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    @Override
    public User getUserById(Long userId) throws SQLException {
        return userDAO.findUserById(userId);
    }

    @Override
    public User getUserByName(String name) throws SQLException {
        return userDAO.findUserByName(name);
    }

    @Override
    public void editProfile(User user) throws SQLException {
        userDAO.editProfile(user);
    }

    @Override
    public void addUser(User user) {

        System.out.println("Service: addUser()");

        userDAO.addUser(user);
    }

    @Override
    public Long getUserIdByUsername(String name) {
        return userDAO.getUserIdByUsername(name);
    }

}
