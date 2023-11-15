package ua.nure.dao.EntityDAO;

import ua.nure.dao.CRUDRepository;
import ua.nure.entity.User;

import java.util.List;

public interface UserDAO extends CRUDRepository<User> {
    List<User> findByMultipleKeys(String login, String password);
}
