package application.domain.services.catalog;

import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConsultPublishedCatalogService {

    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    /** Public catalog: only PUBLISHED products. */
    public List<Product> execute(User requestingUser) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        Product filter = new Product();
        filter.setStatus(ProductStatus.PUBLISHED);
        return productRepositoryPort.findByStatus(filter);
    }

    /** Products of one seller: the owner and administrative roles see all of them; others only published. */
    public List<Product> executeBySeller(User requestingUser, Seller seller) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        List<Product> products = productRepositoryPort.findBySeller(seller);
        boolean owner = SystemRole.SELLER.equals(requestingUser.getRole()) && requestingUser.getParticipant() != null
                && Objects.equals(requestingUser.getParticipant().getIdentification(), seller.getIdentification());
        boolean administrative = SystemRole.ADMINISTRATOR.equals(requestingUser.getRole())
                || SystemRole.SUPERVISOR.equals(requestingUser.getRole());
        if (owner || administrative) {
            return products;
        }
        return products.stream().filter(p -> ProductStatus.PUBLISHED.equals(p.getStatus())).toList();
    }
}
