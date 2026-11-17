package application.domain.ports.out;

import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;

import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepositoryPort {

    ReturnRequest save(ReturnRequest returnRequest);

    Optional<ReturnRequest> findByIdentifier(ReturnRequest returnRequest);

    List<ReturnRequest> findByOrder(Order order);

    List<ReturnRequest> findByBuyer(Buyer buyer);

    List<ReturnRequest> findByStatus(ReturnRequest returnRequest);

    List<ReturnRequest> findAll();

    void update(ReturnRequest returnRequest);
}
