package annotations.handlers.configuration;

import annotations.Column;
import annotations.Entity;
import annotations.GeneratedValue;
import annotations.Id;
import annotations.JoinColumn;
import annotations.ManyToOne;
import annotations.Table;

@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue
    @Column(name = "address_id")
    private Long id;

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

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User userDetails;

    public Address() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public User getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(User userDetails) {
        this.userDetails = userDetails;
    }
}
