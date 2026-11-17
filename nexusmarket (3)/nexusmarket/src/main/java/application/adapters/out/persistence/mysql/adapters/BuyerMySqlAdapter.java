package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.BuyerEntity;
import application.adapters.out.persistence.mysql.mappers.ParticipantPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.BuyerJpaRepository;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Person;
import application.domain.ports.out.BuyerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class BuyerMySqlAdapter implements BuyerRepositoryPort {

    private final BuyerJpaRepository buyerJpaRepository;

    @Override
    public Buyer save(Buyer buyer) {
        if (buyer.getIdentification() == null || buyer.getIdentification().isBlank()) {
            throw new DomainException("The buyer identification is required.");
        }
        BuyerEntity entity = new BuyerEntity();
        ParticipantPersistenceMapper.copy(buyer, entity);
        buyerJpaRepository.save(entity);
        return buyer;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Buyer> findByIdentification(Buyer buyer) {
        if (buyer == null || buyer.getIdentification() == null) {
            return Optional.empty();
        }
        return buyerJpaRepository.findById(buyer.getIdentification()).map(ParticipantPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdentification(Person person) {
        return person != null && person.getIdentification() != null
                && buyerJpaRepository.existsById(person.getIdentification());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(Person person) {
        return person != null && person.getEmail() != null && buyerJpaRepository.existsByEmail(person.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Buyer> findAll() {
        return buyerJpaRepository.findAll().stream().map(ParticipantPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(Buyer buyer) {
        BuyerEntity entity = buyerJpaRepository.findById(buyer.getIdentification())
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
        ParticipantPersistenceMapper.copy(buyer, entity);
        buyerJpaRepository.save(entity);
    }
}
