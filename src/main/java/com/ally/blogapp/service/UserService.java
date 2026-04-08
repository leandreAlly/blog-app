package com.ally.blogapp.service;

import com.ally.blogapp.dao.UserDao;
import com.ally.blogapp.dao.impl.UserDaoImpl;
import com.ally.blogapp.model.User;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDaoImpl();
    }

    public User register(User user) {
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userDao.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        return userDao.save(user);
    }

    public Optional<User> findById(Long id) {
        return userDao.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    public User update(User user) {
        userDao.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userDao.update(user);
    }

    public void delete(Long id) {
        userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userDao.delete(id);
    }
}
