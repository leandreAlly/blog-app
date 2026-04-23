package com.ally.blogapp.service;

import com.ally.blogapp.exception.DuplicateResourceException;
import com.ally.blogapp.exception.InvalidOperationException;
import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.Role;
import com.ally.blogapp.model.User;
import com.ally.blogapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.REPEATABLE_READ,
                   rollbackFor = Exception.class)
    public User register(String username, String email, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already taken: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered: " + email);
        }
        User user = new User(username, email, password, Role.valueOf(role));
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidOperationException("Invalid username or password"));
        if (!user.getPassword().equals(password)) {
            throw new InvalidOperationException("Invalid username or password");
        }
        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public User update(Long id, String bio, String profileImage) {
        User user = findById(id);
        if (bio != null) user.setBio(bio);
        if (profileImage != null) user.setProfileImage(profileImage);
        return userRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void delete(Long id) {
        findById(id);
        userRepository.deleteById(id);
    }
}
