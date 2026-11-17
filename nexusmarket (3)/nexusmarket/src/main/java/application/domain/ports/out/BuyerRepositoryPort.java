package application.domain.ports.out;

import application.domain.models.Buyer;
import application.domain.models.Person;

import java.util.List;
import java.util.Optional;

public interface BuyerRepositoryPort {

    Buyer save(Buyer buyer);

    Optional<Buyer> findByIdentification(Buyer buyer);

    boolean existsByIdentification(Person person);

    boolean existsByEmail(Person person);

    List<Buyer> findAll();

    void update(Buyer buyer);
}
