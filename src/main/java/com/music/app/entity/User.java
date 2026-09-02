package com.music.app.entity;

import com.music.app.enums.Role;

public class User {

    private Long userId;
    private String userName;
    private String phoneNumber;
    private String email;
private String password;
private Role role;

    public User(long id, String username, String email, String password, String phone, Role role) {
        this.userId = id;
        this.userName = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phone;
        this.role = role;
    }
    public User(){}

    public User(String userName, String phoneNumber, String email, String password, Long userId,  Role role) {
    this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
    return userId;
    }
    public void setUserId(Long userId) {
    this.userId = userId;
    }
    public String getUserName() {
    return userName;
    }
    public void setUserName(String userName) {
    this.userName = userName;
    }
    public String getPhoneNumber() {
    return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
    }
    public String getEmail() {
    return email;
    }
    public void setEmail(String email) {
    this.email = email;
    }
    public String getPassword() {
    return password;
    }

    public void setPassword(String password) {
    this.password = password;
    }
    public Role getRole() {
    return role;
    }
    public void setRole(Role role) {
    this.role = role;
    }
}
