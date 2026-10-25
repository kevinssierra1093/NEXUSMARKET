package application.domain.models;

import lombok.Getter;
import lombok.Setter;

import application.domain.valueObjects.userStatus;

@Getter
@Setter


public class User extends Person {

    private Long userId;
    private String username;
    private String password;
    private userStatus status;
    private MarketplaceParticipant participant;

}
