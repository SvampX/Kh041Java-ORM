package annotations.handlers.configuration;

import annotations.Column;
import annotations.Entity;
import annotations.GeneratedValue;
import annotations.Id;
import annotations.ManyToOne;
import annotations.Table;

@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue
    private int id;

    @Column(name = "address_id")
    private String addressId;

    @Column
    private String city;

    @Column
    private String country;

    @Column
    private String streetName;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(length = 10, nullable = false)
    private String type;

    @ManyToOne
//    @JoinColumn(name = "users_id")
    private User userDetails;

    public Address() {
    }

}
