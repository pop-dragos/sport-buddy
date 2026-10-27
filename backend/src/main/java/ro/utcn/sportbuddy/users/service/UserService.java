package ro.utcn.sportbuddy.users.service;

import org.springframework.stereotype.Service;
import ro.utcn.sportbuddy.users.dto.UserSummary;
import ro.utcn.sportbuddy.users.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserSummary> findAll() {
        return userRepository.findAll().stream().map(UserSummary::from).toList();
    }
}