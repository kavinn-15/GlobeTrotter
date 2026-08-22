package com.example.loginapp.service;

import com.example.loginapp.entity.User;
import com.example.loginapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Plain Spring-managed service bean (Spring Core / IoC container).
 * Contains the business logic for registering and authenticating users.
 *
 * NOTE: for clarity in this demo, passwords are stored as plain text.
 * In a real application, hash passwords (e.g. with BCryptPasswordEncoder)
 * before persisting them.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User register(User user) {
        return userRepository.save(user);
    }

    /**
     * Returns the user if the username/password combination is valid.
     */
    public Optional<User> authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(rawPassword));
    }
}
