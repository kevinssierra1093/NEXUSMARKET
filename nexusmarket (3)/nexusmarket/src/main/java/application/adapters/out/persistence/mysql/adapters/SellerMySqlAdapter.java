package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.SellerEntity;
import application.adapters.out.persistence.mysql.mappers.ParticipantPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.SellerJpaRepository;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Person;
import application.domain.models.Seller;
import application.domain.ports.out.SellerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class SellerMySqlAdapter implements SellerRepositoryPort {

    private final SellerJpaRepository sellerJpaRepository;

    @Override
    public Seller save(Seller seller) {
        if (seller.getIdentification() == null || seller.getIdentification().isBlank()) {
            throw new DomainException("The seller identification is required.");
        }
        SellerEntity entity = new SellerEntity();
        ParticipantPersistenceMapper.copy(seller, entity);
        sellerJpaRepository.save(entity);
        return seller;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Seller> findByIdentification(Seller seller) {
        if (seller == null || seller.getIdentification() == null) {
            return Optional.empty();
        }
        return sellerJpaRepository.findById(seller.getIdentification()).map(ParticipantPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdentification(Person person) {
        return person != null && person.getIdentification() != null
                && sellerJpaRepository.existsById(person.getIdentification());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(Person person) {
        return person != null && person.getEmail() != null && sellerJpaRepository.existsByEmail(person.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seller> findAll() {
        return sellerJpaRepository.findAll().stream().map(ParticipantPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(Seller seller) {
        SellerEntity entity = sellerJpaRepository.findById(seller.getIdentification())
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        ParticipantPersistenceMapper.copy(seller, entity);
        sellerJpaRepository.save(entity);
    }
}
