package application.domain.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Buyer extends MarketplaceParticipant {

    private String primaryAddress;
    private String additionalAddress;//direccion adicional 
    private BuyerStatus status;

}