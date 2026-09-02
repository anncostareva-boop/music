package com.music.app.service.implementations;

import com.music.app.config.SecurityConfig;
import com.music.app.dao.interfaces.IUserDAO;
import com.music.app.entity.User;
import com.music.app.service.interfaces.AuthService;
import com.music.app.service.interfaces.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private IUserDAO userDAO;
    private PasswordEncoder passwordEncoder;

    public AuthServiceImpl(IUserDAO userDAO,
                           PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(User user) throws Exception {
        if(usernameExists(user.getUserName())) {
            throw new Exception(
                    "Таке ім'я вже існує"
            );
        }

        if(emailExists(user.getEmail())) {
            throw new Exception(
                    "На таку електронну адресу вже зареєстровано користувача"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        userDAO.addUser(user);
    }

    @Override
    public boolean usernameExists(String username) {
        try{
            return userDAO.findUserByName(username) != null;
        } catch(Exception e) {
        return false;
        }
    }

    @Override
    public boolean emailExists(String email) {
        try{
return userDAO.findUserByEmail(email) != null;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public boolean phoneExists(String phoneNumber) {
        try{
            return userDAO.findUserByPhone(phoneNumber) != null;
        } catch(Exception e) {
            return false;
        }
    }
}
