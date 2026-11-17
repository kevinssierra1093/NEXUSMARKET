package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buyer")
@Getter
@Setter
@NoArgsConstructor
public class BuyerEntity {

    @Id
    @Column(length = 30)
    private String identification;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String phoneNumber;

    @Column(length = 250)
    private String address;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false, length = 250)
    private String primaryAddress;

    @ElementCollection
    @CollectionTable(name = "buyer_additional_address",
            joinColumns = @JoinColumn(name = "buyer_identification"))
    @Column(name = "address", length = 250)
    private List<String> additionalAddresses = new ArrayList<>();

    @Column(nullable = false, length = 30)
    private String status;
}
