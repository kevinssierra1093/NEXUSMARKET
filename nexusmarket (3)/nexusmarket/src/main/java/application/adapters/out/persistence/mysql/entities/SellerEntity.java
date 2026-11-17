package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "seller")
@Getter
@Setter
@NoArgsConstructor
public class SellerEntity {

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

    @Column(nullable = false, length = 150)
    private String businessName;

    private LocalDate registrationDate;

    @Column(nullable = false, length = 30)
    private String status;
}
