package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.UserEntity;
import application.adapters.out.persistence.mysql.mappers.ParticipantPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Person;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserMySqlAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final JpaReferences references;

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity();
        apply(user, entity);
        UserEntity saved = userJpaRepository.save(entity);
        user.setUserId(saved.getUserId());
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(User user) {
        if (user == null || user.getUsername() == null) {
            return Optional.empty();
        }
        return userJpaRepository.findByUsername(user.getUsername()).map(ParticipantPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(User user) {
        if (user == null || user.getUserId() == null) {
            return Optional.empty();
        }
        return userJpaRepository.findById(user.getUserId()).map(ParticipantPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(User user) {
        return user != null && user.getUsername() != null && userJpaRepository.existsByUsername(user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdentification(Person person) {
        return person != null && person.getIdentification() != null
                && userJpaRepository.existsByIdentification(person.getIdentification());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(Person person) {
        return person != null && person.getEmail() != null && userJpaRepository.existsByEmail(person.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userJpaRepository.findAll().stream().map(ParticipantPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(User user) {
        UserEntity entity = userJpaRepository.findById(user.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User"));
        apply(user, entity);
        userJpaRepository.save(entity);
    }

    private void apply(User user, UserEntity entity) {
        ParticipantPersistenceMapper.copy(user, entity);
        entity.setBuyer(user.getParticipant() instanceof Buyer buyer ? references.buyer(buyer) : null);
        entity.setSeller(user.getParticipant() instanceof Seller seller ? references.seller(seller) : null);
    }
}
