package com.music.app.service.implementations;

import com.music.app.config.CustomUserDetails;
import com.music.app.dao.interfaces.IUserDAO;
import com.music.app.entity.User;
import com.music.app.exception.DataNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserDAO userDAO;

    public CustomUserDetailsService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userDAO.findUserByName(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            return new CustomUserDetails(user);
        } catch (DataNotFoundException e) {
            throw new UsernameNotFoundException("User not found: " + username, e);
        }
    }
}