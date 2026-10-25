package application.domain.models;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 

public abstract class DomainCatalog {
    
    private long code;
    private String name;
    private String description;

}