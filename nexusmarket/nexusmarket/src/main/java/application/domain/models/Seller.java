package aplication.domain.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Seller extends MarketplaceParticipant {

    private String BusinessName;//Nombre de la empresa o del vendedor
    private localDate registrationDate;//Fecha de registro del vendedor
    private SellerStatus status;//Estado del vendedor
    private list<warehouse> warehouses;//Lista de almacenes del vendedor
    private list<product> product;//lista de productows del vendedor

}