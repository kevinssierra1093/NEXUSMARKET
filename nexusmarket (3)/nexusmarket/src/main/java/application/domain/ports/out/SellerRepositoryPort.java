package application.domain.ports.out;

import application.domain.models.Person;
import application.domain.models.Seller;

import java.util.List;
import java.util.Optional;

public interface SellerRepositoryPort {

    Seller save(Seller seller);

    Optional<Seller> findByIdentification(Seller seller);

    boolean existsByIdentification(Person person);

    boolean existsByEmail(Person person);

    List<Seller> findAll();

    void update(Seller seller);
}
