package application.domain.ports.out;

import application.domain.models.Refund;
import application.domain.models.ReturnRequest;

import java.util.List;
import java.util.Optional;

public interface RefundRepositoryPort {

    Refund save(Refund refund);

    Optional<Refund> findByIdentifier(Refund refund);

    Optional<Refund> findByReturnRequest(ReturnRequest returnRequest);

    List<Refund> findAll();

    void update(Refund refund);
}
