package annotations.handlers.configuration;

import annotations.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity()
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue()
    @Column(name = "id")
    private int id;

    @Column(name = "brand")
    private String brandName;

    @Column(name = "license_plate")
    private String licensePlate;

    @ManyToMany(tableName = "person_car", inverseJoinColumnsName = "CarId", inverseJoinColumnsReferencedName = "id")
    private List<User> persons = new ArrayList<>();

    public Car(int id, String brandName, String licensePlate) {
        this.id = id;
        this.brandName = brandName;
        this.licensePlate = licensePlate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public List<User> getPersons() {
        return persons;
    }
}
