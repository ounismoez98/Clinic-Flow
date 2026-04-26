package tn.esprit.spring.user;

import java.util.List;

public interface IUserService {

    List<User> getAll();

    User getById(int id);

    User getByUsername(String username);

    List<User> getByRole(Role role);

    User create(User user);

    User update(int id, User user);

    boolean delete(int id);
}
