package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.ReturnRequestEntity;
import application.adapters.out.persistence.mysql.mappers.CommercialPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.ReturnRequestJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static application.adapters.shared.ValueObjectResolver.code;

@Component
@Transactional
@RequiredArgsConstructor
public class ReturnRequestMySqlAdapter implements ReturnRequestRepositoryPort {

    private final ReturnRequestJpaRepository returnRequestJpaRepository;
    private final JpaReferences references;

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        if (returnRequest.getIdentifier() == null) {
            returnRequest.setIdentifier(JpaReferences.newIdentifier());
        }
        ReturnRequestEntity entity = new ReturnRequestEntity();
        entity.setIdentifier(returnRequest.getIdentifier());
        entity.setOrder(references.order(returnRequest.getOrder()));
        entity.setBuyer(references.buyer(returnRequest.getBuyer()));
        entity.setVariant(references.variant(returnRequest.getVariant()));
        CommercialPersistenceMapper.copy(returnRequest, entity);
        returnRequestJpaRepository.save(entity);
        return returnRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnRequest> findByIdentifier(ReturnRequest returnRequest) {
        if (returnRequest == null || returnRequest.getIdentifier() == null) {
            return Optional.empty();
        }
        return returnRequestJpaRepository.findById(returnRequest.getIdentifier())
                .map(CommercialPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findByOrder(Order order) {
        if (order == null || order.getIdentifier() == null) {
            return List.of();
        }
        return returnRequestJpaRepository.findByOrder_Identifier(order.getIdentifier()).stream()
                .map(CommercialPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getIdentification() == null) {
            return List.of();
        }
        return returnRequestJpaRepository.findByBuyer_IdentificationOrderByRequestDateDesc(buyer.getIdentification())
                .stream().map(CommercialPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findByStatus(ReturnRequest returnRequest) {
        return returnRequestJpaRepository.findByStatusOrderByRequestDateAsc(code(returnRequest.getStatus())).stream()
                .map(CommercialPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findAll() {
        return returnRequestJpaRepository.findAll().stream().map(CommercialPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(ReturnRequest returnRequest) {
        ReturnRequestEntity entity = returnRequestJpaRepository.findById(returnRequest.getIdentifier())
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest"));
        CommercialPersistenceMapper.copy(returnRequest, entity);
        returnRequestJpaRepository.save(entity);
    }
}
