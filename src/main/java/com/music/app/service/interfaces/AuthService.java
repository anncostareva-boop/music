package com.music.app.service.interfaces;

import com.music.app.entity.User;

public interface AuthService {

    void register(User user) throws Exception;

    boolean usernameExists(String username);

    boolean emailExists(String email);
    boolean phoneExists(String phoneNumber);
}
