package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByIdentification(String identification);

    boolean existsByEmail(String email);
}
