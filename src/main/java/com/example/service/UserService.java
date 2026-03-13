package com.example.service;

import com.example.entity.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User updateUser(User user);
    User deleteUser(Long id);
    User getUserById(Long id);
    List<User> getAllUsers();
    User getUserByUsername(String username);
    User deactivateUser(Long id);
}
