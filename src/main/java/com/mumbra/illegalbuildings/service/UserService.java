package com.mumbra.illegalbuildings.service;

import com.mumbra.illegalbuildings.model.User;
import com.mumbra.illegalbuildings.model.Role;
import com.mumbra.illegalbuildings.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public Optional<User> getByEmail(String email) {
    return userRepository.findByEmail(email);
}

}
