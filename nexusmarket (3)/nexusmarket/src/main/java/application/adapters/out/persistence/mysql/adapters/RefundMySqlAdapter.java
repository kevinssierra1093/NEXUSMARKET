package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.RefundEntity;
import application.adapters.out.persistence.mysql.mappers.CommercialPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.RefundJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.ports.out.RefundRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class RefundMySqlAdapter implements RefundRepositoryPort {

    private final RefundJpaRepository refundJpaRepository;
    private final JpaReferences references;

    @Override
    public Refund save(Refund refund) {
        if (refund.getIdentifier() == null) {
            refund.setIdentifier(JpaReferences.newIdentifier());
        }
        RefundEntity entity = new RefundEntity();
        entity.setIdentifier(refund.getIdentifier());
        entity.setReturnRequest(references.returnRequest(refund.getReturnRequest()));
        CommercialPersistenceMapper.copy(refund, entity);
        refundJpaRepository.save(entity);
        return refund;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Refund> findByIdentifier(Refund refund) {
        if (refund == null || refund.getIdentifier() == null) {
            return Optional.empty();
        }
        return refundJpaRepository.findById(refund.getIdentifier()).map(CommercialPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Refund> findByReturnRequest(ReturnRequest returnRequest) {
        if (returnRequest == null || returnRequest.getIdentifier() == null) {
            return Optional.empty();
        }
        return refundJpaRepository.findByReturnRequest_Identifier(returnRequest.getIdentifier())
                .map(CommercialPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Refund> findAll() {
        return refundJpaRepository.findAll().stream().map(CommercialPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(Refund refund) {
        RefundEntity entity = refundJpaRepository.findById(refund.getIdentifier())
                .orElseThrow(() -> new EntityNotFoundException("Refund"));
        CommercialPersistenceMapper.copy(refund, entity);
        refundJpaRepository.save(entity);
    }
}
