package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * System identity. A BUYER or SELLER user references its participant through one of the two
 * nullable foreign keys; staff users have both in null.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
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

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne
    @JoinColumn(name = "buyer_identification")
    private BuyerEntity buyer;

    @ManyToOne
    @JoinColumn(name = "seller_identification")
    private SellerEntity seller;
}
