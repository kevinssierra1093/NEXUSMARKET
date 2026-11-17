package application.domain.ports.out;

import application.domain.models.Person;
import application.domain.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByUsername(User user);

    Optional<User> findById(User user);

    boolean existsByUsername(User user);

    boolean existsByIdentification(Person person);

    boolean existsByEmail(Person person);

    List<User> findAll();

    void update(User user);
}
