package application.domain.models;

import lombok.Getter;
import lombok.Setter;

import application.domain.valueObjects.userStatus;

@Getter
@Setter

public class Buyer extends MarketplaceParticipant {

    private String primaryAddress;
    private String additionalAddress;//direccion adicional 
    private userStatus status;

}