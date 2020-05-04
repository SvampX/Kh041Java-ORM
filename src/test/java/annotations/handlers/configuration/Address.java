package annotations.handlers.configuration;

import annotations.Column;
import annotations.Entity;
import annotations.GeneratedValue;
import annotations.GenerationType;
import annotations.Id;
import annotations.Table;

@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @Column(name = "address_id")
    private String addressId;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "street_name")
    private String streetName;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(length = 10, nullable = false)
    private String type;

    public Address() {
    }

}
