package tn.esprit.spring.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User getById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public List<User> getByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(int id, User user) {
        return userRepository.findById(id)
                .map(existing -> {
                    existing.setUsername(user.getUsername());
                    existing.setEmail(user.getEmail());
                    if (user.getPassword() != null && !user.getPassword().isBlank()) {
                        existing.setPassword(user.getPassword());
                    }
                    if (user.getRole() != null) {
                        existing.setRole(user.getRole());
                    }
                    return userRepository.save(existing);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}
