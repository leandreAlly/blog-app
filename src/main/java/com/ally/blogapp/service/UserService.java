package com.ally.blogapp.service;

import com.ally.blogapp.exception.DuplicateResourceException;
import com.ally.blogapp.exception.InvalidOperationException;
import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.Role;
import com.ally.blogapp.model.User;
import com.ally.blogapp.config.CacheConfig;
import com.ally.blogapp.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        User user = new User(username, email, passwordEncoder.encode(password), Role.valueOf(role));
        return userRepository.save(user);
    }

    @Cacheable(value = CacheConfig.USERS, key = "#id")
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidOperationException("Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidOperationException("Invalid username or password");
        }
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.REPEATABLE_READ,
                   rollbackFor = Exception.class)
    public User findOrCreateByOAuth2(String email, String displayName) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
            String username = userRepository.existsByUsername(base)
                    ? base + "_" + UUID.randomUUID().toString().substring(0, 6)
                    : base;
            String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
            return userRepository.save(new User(username, email, randomPassword, Role.READER));
        });
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @CacheEvict(value = CacheConfig.USERS, key = "#id")
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public User update(Long id, String bio, String profileImage) {
        User user = findById(id);
        if (bio != null) user.setBio(bio);
        if (profileImage != null) user.setProfileImage(profileImage);
        return userRepository.save(user);
    }

    @CacheEvict(value = CacheConfig.USERS, key = "#id")
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void delete(Long id) {
        findById(id);
        userRepository.deleteById(id);
    }
}
