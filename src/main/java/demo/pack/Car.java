package demo.pack;

import annotations.Column;
import annotations.Entity;
import annotations.GeneratedValue;
import annotations.Id;
import annotations.ManyToMany;
import annotations.Table;

import java.util.ArrayList;
import java.util.List;

@Entity()
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue
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
