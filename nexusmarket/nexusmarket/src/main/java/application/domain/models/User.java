package application.domain.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class User extends Person {

    private String password;
    private String username;

}
