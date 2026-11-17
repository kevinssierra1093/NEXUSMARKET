package application.domain.models;

import application.domain.valueobjects.SellerStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Seller extends MarketplaceParticipant {
    private String businessName;
    private LocalDate registrationDate;
    private SellerStatus status;
    // Populated on demand; empty by default.
    private List<Warehouse> warehouses = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
}
