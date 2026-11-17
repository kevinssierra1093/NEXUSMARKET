package application.domain.models;

import application.domain.valueobjects.BuyerStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Buyer extends MarketplaceParticipant {
    private String primaryAddress;
    private List<String> additionalAddresses = new ArrayList<>();
    private BuyerStatus status;
}
